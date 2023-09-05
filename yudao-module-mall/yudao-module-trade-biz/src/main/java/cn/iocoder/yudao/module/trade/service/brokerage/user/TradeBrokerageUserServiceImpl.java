package cn.iocoder.yudao.module.trade.service.brokerage.user;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.TradeBrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.user.TradeBrokerageUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.MEMBER_FROZEN_BROKERAGE_PRICE_NOT_ENOUGH;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.BROKERAGE_USER_NOT_EXISTS;

/**
 * 分销用户 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class TradeBrokerageUserServiceImpl implements TradeBrokerageUserService {

    @Resource
    private TradeBrokerageUserMapper brokerageUserMapper;

    private void validateBrokerageUserExists(Long id) {
        if (brokerageUserMapper.selectById(id) == null) {
            throw exception(BROKERAGE_USER_NOT_EXISTS);
        }
    }

    @Override
    public TradeBrokerageUserDO getBrokerageUser(Long id) {
        return brokerageUserMapper.selectById(id);
    }

    @Override
    public List<TradeBrokerageUserDO> getBrokerageUserList(Collection<Long> ids) {
        return brokerageUserMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<TradeBrokerageUserDO> getBrokerageUserPage(TradeBrokerageUserPageReqVO pageReqVO) {
        return brokerageUserMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateBrokerageUserId(Long id, Long brokerageUserId) {
        // 校验存在
        validateBrokerageUserExists(id);

    }

    @Override
    public void updateBrokerageEnabled(Long id, Boolean brokerageEnabled) {
        // 校验存在
        validateBrokerageUserExists(id);
    }

    @Override
    public TradeBrokerageUserDO getInviteBrokerageUser(Long id) {
        return Optional.ofNullable(id)
                .map(this::getBrokerageUser)
                .map(TradeBrokerageUserDO::getBrokerageUserId)
                .map(this::getBrokerageUser)
                .orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserBrokeragePrice(Long id, int brokeragePrice) {
        if (brokeragePrice > 0) {
            brokerageUserMapper.updateBrokeragePriceIncr(id, brokeragePrice);
        } else if (brokeragePrice < 0) {
            brokerageUserMapper.updateBrokeragePriceDecr(id, brokeragePrice);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserFrozenBrokeragePrice(Long id, int frozenBrokeragePrice) {
        if (frozenBrokeragePrice > 0) {
            brokerageUserMapper.updateFrozenBrokeragePriceIncr(id, frozenBrokeragePrice);
        } else if (frozenBrokeragePrice < 0) {
            brokerageUserMapper.updateFrozenBrokeragePriceDecr(id, frozenBrokeragePrice);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFrozenBrokeragePriceDecrAndBrokeragePriceIncr(Long id, int frozenBrokeragePrice) {
        Assert.isTrue(frozenBrokeragePrice < 0);
        int updateRows = brokerageUserMapper.updateFrozenBrokeragePriceDecrAndBrokeragePriceIncr(id, frozenBrokeragePrice);
        if (updateRows == 0) {
            throw exception(MEMBER_FROZEN_BROKERAGE_PRICE_NOT_ENOUGH);
        }
    }

}
