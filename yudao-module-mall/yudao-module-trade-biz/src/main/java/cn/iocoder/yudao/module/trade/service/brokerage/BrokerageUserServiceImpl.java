package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.user.BrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByUserCountRespVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageUserMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageBindModeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageEnabledConditionEnum;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

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
        List<Integer> levels = buildUserQueryLevels(pageReqVO.getBindUserId(), pageReqVO.getLevel());
        return brokerageUserMapper.selectPage(pageReqVO, levels);
    }

    @Override
    public void updateBrokerageUserId(Long id, Long bindUserId) {
        // 校验存在
        BrokerageUserDO brokerageUser = validateBrokerageUserExists(id);

        // 情况一：清除推广员
        if (bindUserId == null) {
            // 清除推广员
            brokerageUserMapper.updateBindUserIdAndBindUserTimeToNull(id);
            return;
        }

        // 绑定关系未发生变化
        // TODO @疯狂：这个放到“情况一”之前，貌似也没关系？
        if (Objects.equals(brokerageUser.getBindUserId(), bindUserId)) {
            return;
        }

        // 情况二：修改推广员
        validateCanBindUser(brokerageUser, bindUserId);
        brokerageUserMapper.updateById(fillBindUserData(bindUserId, new BrokerageUserDO().setId(id)));
    }

    @Override
    public void updateBrokerageUserEnabled(Long id, Boolean enabled) {
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
    public boolean updateUserPrice(Long id, Integer price) {
        if (price > 0) {
            brokerageUserMapper.updatePriceIncr(id, price);
        } else if (price < 0) {
            return brokerageUserMapper.updatePriceDecr(id, price) > 0;
        }
        return true;
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
    public Long getBrokerageUserCountByBindUserId(Long bindUserId, Integer level) {
        List<Integer> levels = buildUserQueryLevels(bindUserId, level);
        if (CollUtil.isEmpty(levels)) {
            return 0L;
        }
        return brokerageUserMapper.selectCountByBindUserIdAndLevelIn(bindUserId, levels);
    }

    @Override
    public boolean bindBrokerageUser(Long userId, Long bindUserId, Boolean isNewUser) {
        // 1. 获得分销用户
        boolean isNewBrokerageUser = false;
        BrokerageUserDO brokerageUser = brokerageUserMapper.selectById(userId);
        if (brokerageUser == null) { // 分销用户不存在的情况：1. 新注册；2. 旧数据；3. 分销功能关闭后又打开
            isNewBrokerageUser = true;
            brokerageUser = new BrokerageUserDO().setId(userId).setBrokerageEnabled(false).setBrokeragePrice(0).setFrozenPrice(0);
        }

        // 2.1 校验是否能绑定用户
        boolean validated = isUserCanBind(brokerageUser, isNewUser);
        if (!validated) {
            return false;
        }
        // 2.3 校验能否绑定
        validateCanBindUser(brokerageUser, bindUserId);
        // 2.3 绑定用户
        if (isNewBrokerageUser) {
            Integer enabledCondition = tradeConfigService.getTradeConfig().getBrokerageEnabledCondition();
            if (BrokerageEnabledConditionEnum.ALL.getCondition().equals(enabledCondition)) { // 人人分销：用户默认就有分销资格
                brokerageUser.setBrokerageEnabled(true).setBrokerageTime(LocalDateTime.now());
            }
            brokerageUser.setBindUserId(bindUserId).setBindUserTime(LocalDateTime.now());
            brokerageUserMapper.insert(fillBindUserData(bindUserId, brokerageUser));
        } else {
            brokerageUserMapper.updateById(fillBindUserData(bindUserId, new BrokerageUserDO().setId(userId)));
        }
        return true;
    }

    private BrokerageUserDO fillBindUserData(Long bindUserId, BrokerageUserDO brokerageUser) {
        BrokerageUserDO bindUser = getBrokerageUser(bindUserId);

        Integer bindUserLevel = 0;
        String bindUserPath = "";
        if (bindUser != null) {
            bindUserLevel = ObjectUtil.defaultIfNull(bindUser.getLevel(), 0);
            bindUserPath = bindUser.getPath();
        }

        String path = StrUtil.isEmpty(bindUserPath)
                ? String.valueOf(bindUserId)
                : String.format("%s,%s", bindUserPath, bindUserId);
        return brokerageUser.setBindUserId(bindUserId).setBindUserTime(LocalDateTime.now())
                .setLevel(bindUserLevel + 1).setPath(path);
    }

    @Override
    public Boolean getUserBrokerageEnabled(Long userId) {
        // 全局分销功能是否开启
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig == null || BooleanUtil.isFalse(tradeConfig.getBrokerageEnabled())) {
            return false;
        }

        // 用户是否有分销资格
        return Optional.ofNullable(getBrokerageUser(userId))
                .map(BrokerageUserDO::getBrokerageEnabled)
                .orElse(false);
    }

    @Override
    public PageResult<AppBrokerageUserRankByUserCountRespVO> getBrokerageUserRankPageByUserCount(AppBrokerageUserRankPageReqVO pageReqVO) {
        IPage<AppBrokerageUserRankByUserCountRespVO> pageResult = brokerageUserMapper.selectCountPageGroupByBindUserId(MyBatisUtils.buildPage(pageReqVO),
                ArrayUtil.get(pageReqVO.getTimes(), 0), ArrayUtil.get(pageReqVO.getTimes(), 1));
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    private boolean isUserCanBind(BrokerageUserDO user, Boolean isNewUser) {
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

        // 下级不能绑定自己的上级
        if (StrUtil.split(bindUser.getPath(), ",").contains(String.valueOf(user.getId()))) {
            throw exception(BROKERAGE_BIND_LOOP);
        }
    }

    // TODO @芋艿：这个层级，要微信讨论下；
    private List<Integer> buildUserQueryLevels(Long bindUserId, Integer level) {
        List<Integer> levels = new ArrayList<>(2);

        BrokerageUserDO bindUser = getBrokerageUser(bindUserId);
        if (bindUser == null) {
            return levels;
        }

        if (level == null) {
            // 默认查两层
            levels.add(bindUser.getLevel() + 1);
            levels.add(bindUser.getLevel() + 2);
        } else {
            levels.add(bindUser.getLevel() + level);
        }
        return levels;

    }

}
