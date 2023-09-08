package cn.iocoder.yudao.module.trade.service.brokerage.user;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.BrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.user.BrokerageUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.BROKERAGE_USER_FROZEN_PRICE_NOT_ENOUGH;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.BROKERAGE_USER_NOT_EXISTS;

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
    public void updateBrokerageUserId(Long id, Long brokerageUserId) {
        // 校验存在
        validateBrokerageUserExists(id);
        // TODO @疯狂：貌似没实现完
    }

    @Override
    public void updateBrokerageEnabled(Long id, Boolean enabled) {
        // 校验存在
        validateBrokerageUserExists(id);
        // TODO @疯狂：貌似没实现完
    }

    private void validateBrokerageUserExists(Long id) {
        if (brokerageUserMapper.selectById(id) == null) {
            throw exception(BROKERAGE_USER_NOT_EXISTS);
        }
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

}
