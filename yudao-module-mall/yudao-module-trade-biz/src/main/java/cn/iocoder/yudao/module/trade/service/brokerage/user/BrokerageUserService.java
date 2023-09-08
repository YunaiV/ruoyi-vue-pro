package cn.iocoder.yudao.module.trade.service.brokerage.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.BrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.BrokerageUserDO;

import java.util.Collection;
import java.util.List;

/**
 * 分销用户 Service 接口
 *
 * @author owen
 */
public interface BrokerageUserService {

    /**
     * 获得分销用户
     *
     * @param id 编号
     * @return 分销用户
     */
    BrokerageUserDO getBrokerageUser(Long id);

    /**
     * 获得分销用户列表
     *
     * @param ids 编号
     * @return 分销用户列表
     */
    List<BrokerageUserDO> getBrokerageUserList(Collection<Long> ids);

    /**
     * 获得分销用户分页
     *
     * @param pageReqVO 分页查询
     * @return 分销用户分页
     */
    PageResult<BrokerageUserDO> getBrokerageUserPage(BrokerageUserPageReqVO pageReqVO);

    /**
     * 修改推广员编号
     *
     * @param id              用户编号
     * @param brokerageUserId 推广员编号
     */
    void updateBrokerageUserId(Long id, Long brokerageUserId);

    /**
     * 修改推广资格
     *
     * @param id               用户编号
     * @param brokerageEnabled 推广资格
     */
    void updateBrokerageEnabled(Long id, Boolean brokerageEnabled);

    /**
     * 获得用户的推广人
     *
     * @param id 用户编号
     * @return 用户的推广人
     */
    BrokerageUserDO getBindBrokerageUser(Long id);

    /**
     * 更新用户佣金
     *
     * @param id             用户编号
     * @param brokeragePrice 用户可用佣金
     */
    void updateUserBrokeragePrice(Long id, Integer brokeragePrice);

    /**
     * 更新用户冻结佣金
     *
     * @param id                   用户编号
     * @param frozenBrokeragePrice 用户冻结佣金
     */
    void updateUserFrozenBrokeragePrice(Long id, Integer frozenBrokeragePrice);

    /**
     * 更新用户冻结佣金（减少）, 更新用户佣金（增加）
     *
     * @param id                   用户编号
     * @param frozenBrokeragePrice 减少冻结佣金（负数）
     */
    void updateFrozenBrokeragePriceDecrAndBrokeragePriceIncr(Long id, Integer frozenBrokeragePrice);

    /**
     * 获得推广用户数量（一级）
     *
     * @param brokerageUserId 推广员编号
     * @return 推广用户数量
     */
    Long getCountByBrokerageUserId(Long brokerageUserId);
}
