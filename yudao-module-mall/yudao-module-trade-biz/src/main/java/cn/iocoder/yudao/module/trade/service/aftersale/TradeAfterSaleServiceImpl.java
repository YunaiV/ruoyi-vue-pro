package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.convert.aftersale.TradeAfterSaleConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.TradeAfterSaleMapper;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

/**
 * 交易售后 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class TradeAfterSaleServiceImpl implements TradeAfterSaleService {

    @Resource
    private TradeOrderService tradeOrderService;

    @Resource
    private TradeAfterSaleMapper tradeAfterSaleMapper;

    @Override
    public Long createAfterSale(Long userId, AppAfterSaleCreateReqVO createReqVO) {
        // 第一步，前置校验
        TradeOrderItemDO tradeOrderItem = validateOrderItemApplicable(userId, createReqVO);

        // 第二步，存储交易售后
        TradeAfterSaleDO afterSale = createAfterSale(createReqVO, tradeOrderItem);
        return afterSale.getId();
    }

    private TradeOrderItemDO validateOrderItemApplicable(Long userId, AppAfterSaleCreateReqVO createReqVO) {
        // 校验订单项存在
        TradeOrderItemDO orderItem = tradeOrderService.getOrderItem(userId, createReqVO.getOrderItemId());
        if (orderItem == null) {
            throw exception(ORDER_ITEM_NOT_FOUND);
        }

        // 已申请售后，不允许再发起售后申请
        if (!TradeOrderItemAfterSaleStatusEnum.isNone(orderItem.getAfterSaleStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_ITEM_APPLIED);
        }
        // TODO 芋艿：超过一定时间，不允许售后

        // 申请的退款金额，不能超过商品的价格
        if (createReqVO.getApplyPrice() > orderItem.getOrderDividePrice()) {
            throw exception(AFTER_SALE_CREATE_FAIL_REFUND_PRICE_ERROR);
        }

        // 校验订单存在
        TradeOrderDO order = tradeOrderService.getOrder(userId, orderItem.getOrderId());
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 已取消，无法发起售后
        if (TradeOrderStatusEnum.isCanceled(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_CANCELED);
        }
        // 未支付，无法发起售后
        if (!TradeOrderStatusEnum.havePaid(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_PAID);
        }
        // 如果是【退货退款】的情况，需要额外校验是否发货
        if (createReqVO.getType().equals(TradeAfterSaleTypeEnum.RETURN_AND_REFUND.getType())
            && !TradeOrderStatusEnum.haveDelivered(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_DELIVERED);
        }
        return orderItem;
    }

    private TradeAfterSaleDO createAfterSale(AppAfterSaleCreateReqVO createReqVO,
                                             TradeOrderItemDO tradeOrderItem) {
        // 创建售后单
        TradeAfterSaleDO afterSale = TradeAfterSaleConvert.INSTANCE.convert(createReqVO, tradeOrderItem);
        afterSale.setNo(RandomUtil.randomString(10)); // TODO 芋艿：优化 no 生成逻辑
        afterSale.setStatus(TradeAfterSaleStatusEnum.APPLY.getStatus());
        // TODO 退还积分
        tradeAfterSaleMapper.insert(afterSale);

        // 更新交易订单项的售后状态 TODO

        // 发送售后消息
        return afterSale;
    }

}
