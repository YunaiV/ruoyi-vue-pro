package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageWithdrawSummaryRespBO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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
     * @param userId      会员用户编号
     * @param createReqVO 创建信息
     * @return 佣金提现编号
     */
    Long createBrokerageWithdraw(Long userId, AppBrokerageWithdrawCreateReqVO createReqVO);

    /**
     * 按照 userId，汇总每个用户的提现
     *
     * @param userIds 用户编号
     * @param status  提现状态
     * @return 用户提现汇总 List
     */
    List<BrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                        BrokerageWithdrawStatusEnum status);

    /**
     * 按照 userId，汇总每个用户的提现
     *
     * @param userIds 用户编号
     * @param status  提现状态
     * @return 用户提现汇总 Map
     */
    default Map<Long, BrokerageWithdrawSummaryRespBO> getWithdrawSummaryMapByUserId(Set<Long> userIds,
                                                                                    BrokerageWithdrawStatusEnum status) {
        return convertMap(getWithdrawSummaryListByUserId(userIds, status), BrokerageWithdrawSummaryRespBO::getUserId);
    }

}
