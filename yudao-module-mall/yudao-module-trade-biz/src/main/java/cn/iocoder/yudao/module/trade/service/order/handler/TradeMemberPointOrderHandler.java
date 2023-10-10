package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.iocoder.yudao.module.member.api.level.MemberLevelApi;
import cn.iocoder.yudao.module.member.api.point.MemberPointApi;
import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 会员积分、等级的 {@link TradeOrderHandler} 实现类
 *
 * @author owen
 */
@Component
public class TradeMemberPointOrderHandler implements TradeOrderHandler {

    @Resource
    private MemberPointApi memberPointApi;
    @Resource
    private MemberLevelApi memberLevelApi;

    @Resource
    private AfterSaleService afterSaleService;

    @Override
    public void afterOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 扣减用户积分（订单抵扣）。不在前置扣减的原因，是因为积分扣减时，需要记录关联业务
        reducePoint(order.getUserId(), order.getUsePoint(), MemberPointBizTypeEnum.ORDER_USE, order.getId());
    }

    @Override
    public void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 增加用户积分（订单赠送）
        addPoint(order.getUserId(), order.getGivePoint(), MemberPointBizTypeEnum.ORDER_GIVE,
                order.getId());

        // 增加用户经验
        memberLevelApi.addExperience(order.getUserId(), order.getPayPrice(), MemberExperienceBizTypeEnum.ORDER.getType(),
                String.valueOf(order.getId()));
    }

    @Override
    public void afterCancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 扣减（回滚）用户积分（订单抵扣）
        addPoint(order.getUserId(), order.getUsePoint(), MemberPointBizTypeEnum.ORDER_CANCEL,
                order.getId());
        // TODO 芋艿：需要校验；如果部分子订单已经售后退款，则不进行整单退；因为已经退了一部分积分了
    }

    @Override
    public void afterCancelOrderItem(TradeOrderDO order, TradeOrderItemDO orderItem) {
        // 扣减（回滚）积分（订单赠送）
        reducePoint(order.getUserId(), orderItem.getGivePoint(), MemberPointBizTypeEnum.AFTER_SALE_DEDUCT_GIVE,
                orderItem.getAfterSaleId());
        // 扣减（回滚）积分：增加用户积分（返还抵扣）
        addPoint(order.getUserId(), orderItem.getUsePoint(), MemberPointBizTypeEnum.AFTER_SALE_REFUND_USED,
                orderItem.getAfterSaleId());

        // 扣减（回滚）用户经验
        AfterSaleDO afterSale = afterSaleService.getAfterSale(orderItem.getAfterSaleId());
        memberLevelApi.addExperience(order.getUserId(), -afterSale.getRefundPrice(), MemberExperienceBizTypeEnum.REFUND.getType(),
                String.valueOf(orderItem.getAfterSaleId()));
    }

    /**
     * 添加用户积分
     * <p>
     * 目前是支付成功后，就会创建积分记录。
     * <p>
     * 业内还有两种做法，可以根据自己的业务调整：
     * 1. 确认收货后，才创建积分记录
     * 2. 支付 or 下单成功时，创建积分记录（冻结），确认收货解冻或者 n 天后解冻
     *
     * @param userId  用户编号
     * @param point   增加积分数量
     * @param bizType 业务编号
     * @param bizId   业务编号
     */
    protected void addPoint(Long userId, Integer point, MemberPointBizTypeEnum bizType, Long bizId) {
        if (point != null && point > 0) {
            memberPointApi.addPoint(userId, point, bizType.getType(), String.valueOf(bizId));
        }
    }

    protected void reducePoint(Long userId, Integer point, MemberPointBizTypeEnum bizType, Long bizId) {
        if (point != null && point > 0) {
            memberPointApi.reducePoint(userId, point, bizType.getType(), String.valueOf(bizId));
        }
    }

}
