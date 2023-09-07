package cn.iocoder.yudao.module.trade.service.brokerage.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.TradeBrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;

import java.util.Collection;
import java.util.List;

// TODO @疯狂：要不去掉 Trade 前缀哈；交易这块，我准备除了 tradeorder 保持下，类似 aftersale，都要取消前缀了；tradeorder 保持的原因，是避免 payorder 和它重复
/**
 * 分销用户 Service 接口
 *
 * @author owen
 */
public interface TradeBrokerageUserService {

    /**
     * 获得分销用户
     *
     * @param id 编号
     * @return 分销用户
     */
    TradeBrokerageUserDO getBrokerageUser(Long id);

    /**
     * 获得分销用户列表
     *
     * @param ids 编号
     * @return 分销用户列表
     */
    List<TradeBrokerageUserDO> getBrokerageUserList(Collection<Long> ids);

    /**
     * 获得分销用户分页
     *
     * @param pageReqVO 分页查询
     * @return 分销用户分页
     */
    PageResult<TradeBrokerageUserDO> getBrokerageUserPage(TradeBrokerageUserPageReqVO pageReqVO);

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
    TradeBrokerageUserDO getInviteBrokerageUser(Long id);

    /**
     * 更新用户佣金
     *
     * @param id             用户编号
     * @param brokeragePrice 用户可用佣金
     */
    void updateUserBrokeragePrice(Long id, int brokeragePrice);

    // TODO @疯狂：int 类型一般不用哈；尽量都用封装类型；不差这点内存哈；
    /**
     * 更新用户冻结佣金
     *
     * @param id                   用户编号
     * @param frozenBrokeragePrice 用户冻结佣金
     */
    void updateUserFrozenBrokeragePrice(Long id, int frozenBrokeragePrice);

    /**
     * 更新用户冻结佣金（减少）, 更新用户佣金（增加）
     *
     * @param id                   用户编号
     * @param frozenBrokeragePrice 减少冻结佣金（负数）
     */
    void updateFrozenBrokeragePriceDecrAndBrokeragePriceIncr(Long id, int frozenBrokeragePrice);

}
