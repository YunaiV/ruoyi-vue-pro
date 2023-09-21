package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;

/**
 * 佣金提现 Service 接口
 *
 * @author 芋道源码
 */
public interface BrokerageWithdrawService {

    /**
     * 【管理员】审核佣金提现
     *
     * @param id          佣金编号
     * @param status      审核状态
     * @param auditReason 驳回原因
     */

    void auditBrokerageWithdraw(Integer id, BrokerageWithdrawStatusEnum status, String auditReason);

    /**
     * 获得佣金提现
     *
     * @param id 编号
     * @return 佣金提现
     */
    BrokerageWithdrawDO getBrokerageWithdraw(Integer id);

    /**
     * 获得佣金提现分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金提现分页
     */
    PageResult<BrokerageWithdrawDO> getBrokerageWithdrawPage(BrokerageWithdrawPageReqVO pageReqVO);

    /**
     * 【会员】创建佣金提现
     *
     * @param createReqVO 创建信息
     * @param userId      会员用户编号
     * @return 佣金提现编号
     */
    Long createBrokerageWithdraw(AppBrokerageWithdrawCreateReqVO createReqVO, Long userId);
}
