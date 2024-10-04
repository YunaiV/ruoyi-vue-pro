package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderSummaryRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singleton;

/**
 * 交易订单【读】 Service 接口
 *
 * @author 芋道源码
 */
public interface TradeOrderQueryService {

    // =================== Order ===================

    /**
     * 获得指定编号的交易订单
     *
     * @param id 交易订单编号
     * @return 交易订单
     */
    TradeOrderDO getOrder(Long id);

    /**
     * 获得指定用户，指定的交易订单
     *
     * @param userId 用户编号
     * @param id     交易订单编号
     * @return 交易订单
     */
    TradeOrderDO getOrder(Long userId, Long id);

    /**
     * 获得指定用户，指定活动，指定状态的交易订单
     *
     * @param userId                用户编号
     * @param combinationActivityId 活动编号
     * @param status                订单状态
     * @return 交易订单
     */
    TradeOrderDO getOrderByUserIdAndStatusAndCombination(Long userId, Long combinationActivityId, Integer status);

    /**
     * 获得订单列表
     *
     * @param ids 订单编号数组
     * @return 订单列表
     */
    List<TradeOrderDO> getOrderList(Collection<Long> ids);

    /**
     * 【管理员】获得交易订单分页
     *
     * @param reqVO 分页请求
     * @return 交易订单
     */
    PageResult<TradeOrderDO> getOrderPage(TradeOrderPageReqVO reqVO);

    /**
     * 获得订单统计
     *
     * @param reqVO 请求参数
     * @return 订单统计
     */
    TradeOrderSummaryRespVO getOrderSummary(TradeOrderPageReqVO reqVO);

    /**
     * 【会员】获得交易订单分页
     *
     * @param userId 用户编号
     * @param reqVO  分页请求
     * @return 交易订单
     */
    PageResult<TradeOrderDO> getOrderPage(Long userId, AppTradeOrderPageReqVO reqVO);

    /**
     * 【会员】获得交易订单数量
     *
     * @param userId       用户编号
     * @param status       订单状态。如果为空，则不进行筛选
     * @param commonStatus 评价状态。如果为空，则不进行筛选
     * @return 订单数量
     */
    Long getOrderCount(Long userId, Integer status, Boolean commonStatus);

    /**
     * 【前台】获得订单的物流轨迹
     *
     * @param id     订单编号
     * @param userId 用户编号
     * @return 物流轨迹数组
     */
    List<ExpressTrackRespDTO> getExpressTrackList(Long id, Long userId);

    /**
     * 【后台】获得订单的物流轨迹
     *
     * @param id 订单编号
     * @return 物流轨迹数组
     */
    List<ExpressTrackRespDTO> getExpressTrackList(Long id);

    /**
     * 【会员】在指定秒杀活动下，用户购买的商品数量
     *
     * @param userId     用户编号
     * @param activityId 活动编号
     * @return 秒杀商品数量
     */
    int getActivityProductCount(Long userId, Long activityId);

    // =================== Order Item ===================

    /**
     * 获得指定用户，指定的交易订单项
     *
     * @param userId 用户编号
     * @param itemId 交易订单项编号
     * @return 交易订单项
     */
    TradeOrderItemDO getOrderItem(Long userId, Long itemId);

    /**
     * 获得交易订单项
     *
     * @param id 交易订单项编号 itemId
     * @return 交易订单项
     */
    TradeOrderItemDO getOrderItem(Long id);

    /**
     * 根据交易订单编号，查询交易订单项
     *
     * @param orderId 交易订单编号
     * @return 交易订单项数组
     */
    default List<TradeOrderItemDO> getOrderItemListByOrderId(Long orderId) {
        return getOrderItemListByOrderId(singleton(orderId));
    }

    /**
     * 根据交易订单编号数组，查询交易订单项
     *
     * @param orderIds 交易订单编号数组
     * @return 交易订单项数组
     */
    List<TradeOrderItemDO> getOrderItemListByOrderId(Collection<Long> orderIds);

}
