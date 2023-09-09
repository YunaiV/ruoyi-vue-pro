package cn.iocoder.yudao.module.trade.service.brokerage.user;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.BrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.user.BrokerageUserMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageBindModeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageEnabledConditionEnum;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

/**
 * 分销用户 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class BrokerageUserServiceImpl implements BrokerageUserService {

    @Resource
    private BrokerageUserMapper brokerageUserMapper;

    @Resource
    private TradeConfigService tradeConfigService;

    @Override
    public BrokerageUserDO getBrokerageUser(Long id) {
        return brokerageUserMapper.selectById(id);
    }

    @Override
    public List<BrokerageUserDO> getBrokerageUserList(Collection<Long> ids) {
        return brokerageUserMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<BrokerageUserDO> getBrokerageUserPage(BrokerageUserPageReqVO pageReqVO) {
        return brokerageUserMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateBrokerageUserId(Long id, Long bindUserId) {
        // 0. 校验存在
        BrokerageUserDO brokerageUser = validateBrokerageUserExists(id);
        if (bindUserId == null) {
            // 1. 清除推广员
            brokerageUserMapper.updateBindUserIdAndBindUserTimeToNull(id);
            return;
        }

        // 2.1 校验能否绑定
        validateCanBindUser(brokerageUser, bindUserId);
        // 2.2 修改推广员
        brokerageUserMapper.updateById(new BrokerageUserDO().setId(id)
                .setBindUserId(bindUserId).setBindUserTime(LocalDateTime.now()));
    }

    @Override
    public void updateBrokerageEnabled(Long id, Boolean enabled) {
        // 校验存在
        validateBrokerageUserExists(id);
        if (BooleanUtil.isTrue(enabled)) {
            // 开通推广资格
            brokerageUserMapper.updateById(new BrokerageUserDO().setId(id)
                    .setBrokerageEnabled(true).setBrokerageTime(LocalDateTime.now()));
        } else {
            // 取消推广资格
            brokerageUserMapper.updateEnabledFalseAndBrokerageTimeToNull(id);
        }
    }

    private BrokerageUserDO validateBrokerageUserExists(Long id) {
        BrokerageUserDO brokerageUserDO = brokerageUserMapper.selectById(id);
        if (brokerageUserDO == null) {
            throw exception(BROKERAGE_USER_NOT_EXISTS);
        }

        return brokerageUserDO;
    }

    @Override
    public BrokerageUserDO getBindBrokerageUser(Long id) {
        return Optional.ofNullable(id)
                .map(this::getBrokerageUser)
                .map(BrokerageUserDO::getBindUserId)
                .map(this::getBrokerageUser)
                .orElse(null);
    }

    @Override
    public void updateUserPrice(Long id, Integer price) {
        if (price > 0) {
            brokerageUserMapper.updatePriceIncr(id, price);
        } else if (price < 0) {
            brokerageUserMapper.updatePriceDecr(id, price);
        }
    }

    @Override
    public void updateUserFrozenPrice(Long id, Integer frozenPrice) {
        if (frozenPrice > 0) {
            brokerageUserMapper.updateFrozenPriceIncr(id, frozenPrice);
        } else if (frozenPrice < 0) {
            brokerageUserMapper.updateFrozenPriceDecr(id, frozenPrice);
        }
    }

    @Override
    public void updateFrozenPriceDecrAndPriceIncr(Long id, Integer frozenPrice) {
        Assert.isTrue(frozenPrice < 0);
        int updateRows = brokerageUserMapper.updateFrozenPriceDecrAndPriceIncr(id, frozenPrice);
        if (updateRows == 0) {
            throw exception(BROKERAGE_USER_FROZEN_PRICE_NOT_ENOUGH);
        }
    }

    @Override
    public Long getCountByBindUserId(Long bindUserId) {
        return brokerageUserMapper.selectCount(BrokerageUserDO::getBindUserId, bindUserId);
    }

    @Override
    public boolean bindUser(Long userId, Long bindUserId, Boolean isNewUser) {
        if (userId == null) {
            throw exception(0);
        }

        boolean isInsert = false;
        BrokerageUserDO brokerageUser = brokerageUserMapper.selectById(userId);
        // 分销用户不存在的情况：1.新注册 2.旧数据 3.分销功能关闭后又打开
        if (brokerageUser == null) {
            isInsert = true;
            brokerageUser = new BrokerageUserDO().setId(userId).setBrokerageEnabled(false).setPrice(0).setFrozenPrice(0);
        }

        // 校验分配配置
        boolean validated = validateTradeConfig(brokerageUser, bindUserId, isNewUser);
        if (!validated) {
            return false;
        }

        // 校验能否绑定
        validateCanBindUser(brokerageUser, bindUserId);

        if (isInsert) {
            Integer enabledCondition = tradeConfigService.getTradeConfig().getBrokerageEnabledCondition();
            if (BrokerageEnabledConditionEnum.ALL.getCondition().equals(enabledCondition)) {
                // 人人分销：用户默认就有分销资格
                brokerageUser.setBrokerageEnabled(true).setBindUserTime(LocalDateTime.now());
            }
            brokerageUserMapper.insert(brokerageUser);
        } else {
            brokerageUserMapper.updateById(new BrokerageUserDO().setId(userId)
                    .setBindUserId(bindUserId).setBindUserTime(LocalDateTime.now()));
        }
        return true;
    }

    private boolean validateTradeConfig(BrokerageUserDO user, Long bindUserId, Boolean isNewUser) {
        // 校验分销功能是否启用
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig == null || !BooleanUtil.isTrue(tradeConfig.getBrokerageEnabled())) {
            return false;
        }

        // 校验分佣模式：仅可后台手动设置推广员
        if (BrokerageEnabledConditionEnum.ADMIN.getCondition().equals(tradeConfig.getBrokerageEnabledCondition())) {
            throw exception(BROKERAGE_BIND_CONDITION_ADMIN);
        }

        // 校验分销关系绑定模式
        if (BrokerageBindModeEnum.REGISTER.getMode().equals(tradeConfig.getBrokerageBindMode())) {
            if (!BooleanUtil.isTrue(isNewUser)) {
                throw exception(BROKERAGE_BIND_MODE_REGISTER); // 只有在注册时可以绑定
            }
        } else if (BrokerageBindModeEnum.ANYTIME.getMode().equals(tradeConfig.getBrokerageBindMode())) {
            if (user.getBindUserId() != null) {
                throw exception(BROKERAGE_BIND_OVERRIDE); // 已绑定了推广人
            }
        }

        validateCanBindUser(user, bindUserId);
        return true;
    }

    private void validateCanBindUser(BrokerageUserDO user, Long bindUserId) {
        // 校验要绑定的用户有无推广资格
        BrokerageUserDO bindUser = brokerageUserMapper.selectById(bindUserId);
        if (bindUser == null || !BooleanUtil.isTrue(bindUser.getBrokerageEnabled())) {
            throw exception(BROKERAGE_BIND_USER_NOT_ENABLED);
        }

        // 校验绑定自己
        if (Objects.equals(user.getId(), bindUserId)) {
            throw exception(BROKERAGE_BIND_SELF);
        }

        // A->B->A：下级不能绑定自己的上级,   A->B->C->A可以!!
        if (Objects.equals(user.getId(), bindUser.getBindUserId())) {
            throw exception(BROKERAGE_BIND_LOOP);
        }
    }

}
