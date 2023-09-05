package cn.iocoder.yudao.module.trade.service.brokerage.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.TradeBrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;

import java.util.Collection;
import java.util.List;

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
}
