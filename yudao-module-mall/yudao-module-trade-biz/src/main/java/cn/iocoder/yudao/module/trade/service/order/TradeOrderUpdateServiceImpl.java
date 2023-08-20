package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.member.api.address.AddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.product.api.comment.ProductCommentApi;
import cn.iocoder.yudao.module.product.api.comment.dto.ProductCommentCreateReqDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.promotion.api.bargain.BargainRecordApi;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordUpdateStatusReqDTO;
import cn.iocoder.yudao.module.promotion.api.coupon.CouponApi;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponUseReqDTO;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemCommentCreateReqVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.TradeCartDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.*;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.cart.TradeCartService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.message.TradeMessageService;
import cn.iocoder.yudao.module.trade.service.message.bo.TradeOrderMessageWhenDeliveryOrderReqBO;
import cn.iocoder.yudao.module.trade.service.price.TradePriceService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.ORDER_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

/**
 * 交易订单【写】Service 实现类
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Service
@Slf4j
public class TradeOrderUpdateServiceImpl implements TradeOrderUpdateService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;

    @Resource
    private TradeCartService tradeCartService;
    @Resource
    private TradePriceService tradePriceService;
    @Resource
    private DeliveryExpressService deliveryExpressService;

    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private AddressApi addressApi;
    @Resource
    private CouponApi couponApi;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private ProductCommentApi productCommentApi;
    @Resource
    private TradeMessageService tradeMessageService;
    @Resource
    private TradeOrderProperties tradeOrderProperties;

    @Resource
    private CombinationRecordApi combinationRecordApi;

    @Resource
    private BargainRecordApi bargainRecordApi;

    // =================== Order ===================

    @Override
    public AppTradeOrderSettlementRespVO settlementOrder(Long userId, AppTradeOrderSettlementReqVO settlementReqVO) {
        // 1. 获得收货地址
        AddressRespDTO address = getAddress(userId, settlementReqVO.getAddressId());
        if (address != null) {
            settlementReqVO.setAddressId(address.getId());
        }

        // 2. 计算价格
        TradePriceCalculateRespBO calculateRespBO = calculatePrice(userId, settlementReqVO);

        // 3. 拼接返回
        return TradeOrderConvert.INSTANCE.convert(calculateRespBO, address);
    }

    /**
     * 获得用户地址
     *
     * @param userId    用户编号
     * @param addressId 地址编号
     * @return 地址
     */
    private AddressRespDTO getAddress(Long userId, Long addressId) {
        if (addressId != null) {
            return addressApi.getAddress(addressId, userId);
        }
        return addressApi.getDefaultAddress(userId);
    }

    /**
     * 计算订单价格
     *
     * @param userId          用户编号
     * @param settlementReqVO 结算信息
     * @return 订单价格
     */
    private TradePriceCalculateRespBO calculatePrice(Long userId, AppTradeOrderSettlementReqVO settlementReqVO) {
        // 1. 如果来自购物车，则获得购物车的商品
        List<TradeCartDO> cartList = tradeCartService.getCartList(userId,
                convertSet(settlementReqVO.getItems(), AppTradeOrderSettlementReqVO.Item::getCartId));

        // 2. 计算价格
        TradePriceCalculateReqBO calculateReqBO = TradeOrderConvert.INSTANCE.convert(userId, settlementReqVO, cartList);
        calculateReqBO.getItems().forEach(item -> Assert.isTrue(item.getSelected(), // 防御性编程，保证都是选中的
                "商品({}) 未设置为选中", item.getSkuId()));
        return tradePriceService.calculatePrice(calculateReqBO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrderDO createOrder(Long userId, String userIp, AppTradeOrderCreateReqVO createReqVO) {
        // 2. 价格计算
        TradePriceCalculateRespBO calculateRespBO = calculatePrice(userId, createReqVO);
        // 3.1 插入 TradeOrderDO 订单
        TradeOrderDO order = createTradeOrder(userId, userIp, createReqVO, calculateRespBO);
        // 3.2 插入 TradeOrderItemDO 订单项
        List<TradeOrderItemDO> orderItems = createTradeOrderItems(order, calculateRespBO);
        // 订单创建完后的逻辑
        afterCreateTradeOrder(userId, createReqVO, order, orderItems, calculateRespBO);
        // 3.3 校验订单类型
        // 拼团
        if (Objects.equals(TradeOrderTypeEnum.COMBINATION.getType(), order.getType())) {
            MemberUserRespDTO user = memberUserApi.getUser(userId);
            List<CombinationRecordRespDTO> recordRespDTOS = combinationRecordApi.getRecordListByUserIdAndActivityId(userId, createReqVO.getCombinationActivityId());
            // TODO 拼团一次应该只能选择一种规格的商品
            TradeOrderItemDO orderItemDO = orderItems.get(0);
            if (CollUtil.isNotEmpty(recordRespDTOS)) {
                List<Long> skuIds = convertList(recordRespDTOS, CombinationRecordRespDTO::getSkuId, item -> ObjectUtil.equals(item.getStatus(), CombinationRecordStatusEnum.SUCCESS.getStatus()));
                List<TradeOrderItemDO> tradeOrderItemDOS = tradeOrderItemMapper.selectListByOrderIdAnSkuId(convertList(recordRespDTOS,
                        CombinationRecordRespDTO::getOrderId, item -> ObjectUtil.equals(item.getStatus(), CombinationRecordStatusEnum.SUCCESS.getStatus())), skuIds);
                combinationRecordApi.validateCombinationLimitCount(createReqVO.getCombinationActivityId(),
                        CollectionUtils.getSumValue(tradeOrderItemDOS, TradeOrderItemDO::getCount, Integer::sum), orderItemDO.getCount());
            }

            combinationRecordApi.createCombinationRecord(TradeOrderConvert.INSTANCE.convert(order, orderItemDO, createReqVO, user));
        }
        // TODO 秒杀扣减库存是下单就扣除还是等待订单支付成功再扣除
        if (Objects.equals(TradeOrderTypeEnum.SECKILL.getType(), order.getType())) {

        }

        // TODO @LeeYan9: 是可以思考下, 订单的营销优惠记录, 应该记录在哪里, 微信讨论起来!
        return order;
    }

    /**
     * 校验收件地址是否存在
     *
     * @param userId    用户编号
     * @param addressId 收件地址编号
     * @return 收件地址
     */
    private AddressRespDTO validateAddress(Long userId, Long addressId) {
        AddressRespDTO address = addressApi.getAddress(addressId, userId);
        if (address == null) {
            throw exception(ErrorCodeConstants.ORDER_CREATE_ADDRESS_NOT_FOUND);
        }
        return address;
    }

    private TradeOrderDO createTradeOrder(Long userId, String clientIp, AppTradeOrderCreateReqVO createReqVO,
                                          TradePriceCalculateRespBO calculateRespBO) {
        // 用户选择物流配送的时候才需要填写收货地址
        AddressRespDTO address = new AddressRespDTO();
        if (Objects.equals(createReqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getMode())) {
            // 用户收件地址的校验
            address = validateAddress(userId, createReqVO.getAddressId());
        }
        TradeOrderDO order = TradeOrderConvert.INSTANCE.convert(userId, clientIp, createReqVO, calculateRespBO, address);
        order.setType(validateActivity(createReqVO));
        order.setNo(IdUtil.getSnowflakeNextId() + ""); // TODO @LeeYan9: 思考下, 怎么生成好点哈; 这个是会展示给用户的;
        order.setStatus(TradeOrderStatusEnum.UNPAID.getStatus());
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
        order.setProductCount(getSumValue(calculateRespBO.getItems(), TradePriceCalculateRespBO.OrderItem::getCount, Integer::sum));
        order.setTerminal(TerminalEnum.H5.getTerminal()); // todo 数据来源?
        // 支付信息
        order.setAdjustPrice(0).setPayStatus(false);
        // 物流信息
        order.setDeliveryType(createReqVO.getDeliveryType());
        // 退款信息
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus()).setRefundPrice(0);
        tradeOrderMapper.insert(order);
        return order;
    }

    /**
     * 校验活动，并返回订单类型
     *
     * @param createReqVO 请求参数
     * @return 订单类型
     */
    private Integer validateActivity(AppTradeOrderCreateReqVO createReqVO) {
        if (createReqVO.getSeckillActivityId() != null) {
            return TradeOrderTypeEnum.SECKILL.getType();
        }
        if (createReqVO.getCombinationActivityId() != null) {
            return TradeOrderTypeEnum.COMBINATION.getType();
        }
        // TODO 砍价敬请期待
        return TradeOrderTypeEnum.NORMAL.getType();
    }

    private List<TradeOrderItemDO> createTradeOrderItems(TradeOrderDO tradeOrderDO, TradePriceCalculateRespBO calculateRespBO) {
        List<TradeOrderItemDO> orderItems = TradeOrderConvert.INSTANCE.convertList(tradeOrderDO, calculateRespBO);
        tradeOrderItemMapper.insertBatch(orderItems);
        return orderItems;
    }

    /**
     * 执行创建完创建完订单后的逻辑
     *
     * 例如说：优惠劵的扣减、积分的扣减、支付单的创建等等
     *
     * @param userId          用户编号
     * @param createReqVO     创建订单请求
     * @param tradeOrderDO    交易订单
     * @param calculateRespBO 订单价格计算结果
     */
    private void afterCreateTradeOrder(Long userId, AppTradeOrderCreateReqVO createReqVO,
                                       TradeOrderDO tradeOrderDO, List<TradeOrderItemDO> orderItems,
                                       TradePriceCalculateRespBO calculateRespBO) {
        // 下单时扣减商品库存
        productSkuApi.updateSkuStock(new ProductSkuUpdateStockReqDTO(TradeOrderConvert.INSTANCE.convertList(orderItems)));

        // 删除购物车商品 TODO 芋艿：待实现

        // 扣减积分，抵扣金额 TODO 芋艿：待实现

        // 有使用优惠券时更新
        if (createReqVO.getCouponId() != null) {
            couponApi.useCoupon(new CouponUseReqDTO().setId(createReqVO.getCouponId()).setUserId(userId)
                    .setOrderId(tradeOrderDO.getId()));
        }

        // 生成预支付
        createPayOrder(tradeOrderDO, orderItems, calculateRespBO);

        // 增加订单日志 TODO 芋艿：待实现
    }

    private void createPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems, TradePriceCalculateRespBO calculateRespBO) {
        // 创建支付单，用于后续的支付
        PayOrderCreateReqDTO payOrderCreateReqDTO = TradeOrderConvert.INSTANCE.convert(
                order, orderItems, calculateRespBO, tradeOrderProperties);
        Long payOrderId = payOrderApi.createOrder(payOrderCreateReqDTO);

        // 更新到交易单上
        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId()).setPayOrderId(payOrderId));
        order.setPayOrderId(payOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderPaid(Long id, Long payOrderId) {
        // 校验并获得交易订单（可支付）
        KeyValue<TradeOrderDO, PayOrderRespDTO> orderResult = validateOrderPayable(id, payOrderId);
        TradeOrderDO order = orderResult.getKey();
        PayOrderRespDTO payOrder = orderResult.getValue();

        // 更新 TradeOrderDO 状态为已支付，等待发货
        int updateCount = tradeOrderMapper.updateByIdAndStatus(id, order.getStatus(),
                new TradeOrderDO().setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus()).setPayStatus(true)
                        .setPayTime(LocalDateTime.now()).setPayChannelCode(payOrder.getChannelCode()));
        if (updateCount == 0) {
            throw exception(ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
        // 校验活动
        // 1、拼团活动
        if (Objects.equals(TradeOrderTypeEnum.COMBINATION.getType(), order.getType())) {
            // 更新拼团状态 TODO puhui999：订单支付失败或订单支付过期删除这条拼团记录
            combinationRecordApi.updateCombinationRecordStatus(new CombinationRecordUpdateStatusReqDTO().setUserId(order.getUserId())
                    .setOrderId(order.getId()).setStatus(CombinationRecordStatusEnum.IN_PROGRESS.getStatus()).setStartTime(LocalDateTime.now()));
        }
        // TODO 芋艿：发送订单变化的消息

        // TODO 芋艿：发送站内信

        // TODO 芋艿：OrderLog
    }

    /**
     * 校验交易订单满足被支付的条件
     *
     * 1. 交易订单未支付
     * 2. 支付单已支付
     *
     * @param id         交易订单编号
     * @param payOrderId 支付订单编号
     * @return 交易订单
     */
    private KeyValue<TradeOrderDO, PayOrderRespDTO> validateOrderPayable(Long id, Long payOrderId) {
        // 校验订单是否存在
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 校验订单未支付
        if (!TradeOrderStatusEnum.isUnpaid(order.getStatus()) || order.getPayStatus()) {
            log.error("[validateOrderPaid][order({}) 不处于待支付状态，请进行处理！order 数据是：{}]",
                    id, JsonUtils.toJsonString(order));
            throw exception(ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
        // 校验支付订单匹配
        if (ObjectUtil.notEqual(order.getPayOrderId(), payOrderId)) { // 支付单号
            log.error("[validateOrderPaid][order({}) 支付单不匹配({})，请进行处理！order 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(order));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }

        // 校验支付单是否存在
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[validateOrderPaid][order({}) payOrder({}) 不存在，请进行处理！]", id, payOrderId);
            throw exception(ORDER_NOT_FOUND);
        }
        // 校验支付单已支付
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validateOrderPaid][order({}) payOrder({}) 未支付，请进行处理！payOrder 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        // 校验支付金额一致
        if (ObjectUtil.notEqual(payOrder.getPrice(), order.getPayPrice())) {
            log.error("[validateOrderPaid][order({}) payOrder({}) 支付金额不匹配，请进行处理！order 数据是：{}，payOrder 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(order), JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 校验支付订单匹配（二次）
        if (ObjectUtil.notEqual(payOrder.getMerchantOrderId(), id.toString())) {
            log.error("[validateOrderPaid][order({}) 支付单不匹配({})，请进行处理！payOrder 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }
        return new KeyValue<>(order, payOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliveryOrder(Long userId, TradeOrderDeliveryReqVO deliveryReqVO) {
        // TODO @puhui999：只有选择快递的，才可以发货
        // 1.1 校验并获得交易订单（可发货）
        TradeOrderDO order = validateOrderDeliverable(deliveryReqVO.getId());
        TradeOrderDO updateOrderObj = new TradeOrderDO();
        // 判断发货类型
        // 2.1 快递发货
        if (Objects.equals(deliveryReqVO.getType(), DeliveryTypeEnum.EXPRESS.getMode())) {
            // 校验快递公司
            DeliveryExpressDO deliveryExpress = deliveryExpressService.getDeliveryExpress(deliveryReqVO.getLogisticsId());
            if (deliveryExpress == null) {
                throw exception(EXPRESS_NOT_EXISTS);
            }
            if (deliveryExpress.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) {
                throw exception(EXPRESS_STATUS_NOT_ENABLE);
            }
            updateOrderObj.setLogisticsId(deliveryReqVO.getLogisticsId()).setLogisticsNo(deliveryReqVO.getLogisticsNo()).setDeliveryType(DeliveryTypeEnum.EXPRESS.getMode());
        }
        // TODO @puhui999：无需发货时，更新 logisticsId 为 0；
        // 2.2 无需发货
        if (Objects.equals(deliveryReqVO.getType(), DeliveryTypeEnum.NULL.getMode())) {
            updateOrderObj.setLogisticsId(null).setLogisticsNo("").setDeliveryType(DeliveryTypeEnum.NULL.getMode());
        }

        // 更新 TradeOrderDO 状态为已发货，等待收货
        updateOrderObj.setStatus(TradeOrderStatusEnum.DELIVERED.getStatus()).setDeliveryTime(LocalDateTime.now());
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), updateOrderObj);
        if (updateCount == 0) {
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }
        // TODO 芋艿：发送订单变化的消息

        // 发送站内信
        tradeMessageService.sendMessageWhenDeliveryOrder(new TradeOrderMessageWhenDeliveryOrderReqBO().setOrderId(order.getId())
                .setUserId(userId).setMessage(null));

        // TODO 芋艿：OrderLog
        // TODO 设计：lili：是不是发货后，才支持售后？
    }

    /**
     * 校验交易订单满足被发货的条件
     *
     * 1. 交易订单未发货
     *
     * @param id 交易订单编号
     * @return 交易订单
     */
    private TradeOrderDO validateOrderDeliverable(Long id) {
        // 校验订单是否存在
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 校验订单是否是待发货状态
        if (!TradeOrderStatusEnum.isUndelivered(order.getStatus())) {
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }
        // 校验订单是否退款
        if (ObjectUtil.notEqual(TradeOrderRefundStatusEnum.NONE.getStatus(), order.getRefundStatus())) {
            throw exception(ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE);
        }
        // 订单类型：拼团
        if (Objects.equals(TradeOrderTypeEnum.COMBINATION.getType(), order.getType())) {
            // 校验订单拼团是否成功
            if (combinationRecordApi.isCombinationRecordSuccess(order.getUserId(), order.getId())) {
                throw exception(ORDER_DELIVERY_FAIL_COMBINATION_RECORD_STATUS_NOT_SUCCESS);
            }
        }
        // 订单类类型：砍价
        if (Objects.equals(TradeOrderTypeEnum.BARGAIN.getType(), order.getType())) {
            // 校验订单砍价是否成功
            if (bargainRecordApi.isBargainRecordSuccess(order.getUserId(), order.getId())) {
                throw exception(ORDER_DELIVERY_FAIL_BARGAIN_RECORD_STATUS_NOT_SUCCESS);
            }
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveOrder(Long userId, Long id) {
        // 校验并获得交易订单（可收货）
        TradeOrderDO order = validateOrderReceivable(userId, id);

        // 更新 TradeOrderDO 状态为已完成
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(),
                new TradeOrderDO().setStatus(TradeOrderStatusEnum.COMPLETED.getStatus()).setReceiveTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }
        // TODO 芋艿：OrderLog

        // TODO 芋艿：lili 发送订单变化的消息

        // TODO 芋艿：lili 发送商品被购买完成的数据
    }

    /**
     * 校验交易订单满足可售货的条件
     *
     * 1. 交易订单待收货
     *
     * @param userId 用户编号
     * @param id     交易订单编号
     * @return 交易订单
     */
    private TradeOrderDO validateOrderReceivable(Long userId, Long id) {
        // 校验订单是否存在
        TradeOrderDO order = tradeOrderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 校验订单是否是待收货状态
        if (!TradeOrderStatusEnum.isDelivered(order.getStatus())) {
            throw exception(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }
        return order;
    }

    // =================== Order Item ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderItemAfterSaleStatus(Long id, Integer oldAfterSaleStatus, Integer newAfterSaleStatus,
                                               Long afterSaleId, Integer refundPrice) {
        // 如果退款成功，则 refundPrice 非空
        if (Objects.equals(newAfterSaleStatus, TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus())
                && refundPrice == null) {
            throw new IllegalArgumentException(StrUtil.format("id({}) 退款成功，退款金额不能为空", id));
        }
        // 如果退款发起，则 afterSaleId 非空
        if (Objects.equals(newAfterSaleStatus, TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus())
                && afterSaleId == null) {
            throw new IllegalArgumentException(StrUtil.format("id({}) 退款发起，售后单编号不能为空", id));
        }

        // 更新订单项
        int updateCount = tradeOrderItemMapper.updateAfterSaleStatus(id, oldAfterSaleStatus, newAfterSaleStatus, afterSaleId);
        if (updateCount <= 0) {
            throw exception(ORDER_ITEM_UPDATE_AFTER_SALE_STATUS_FAIL);
        }

        // 如果有退款金额，则需要更新订单
        if (refundPrice == null) {
            return;
        }
        // 计算总的退款金额
        TradeOrderDO order = tradeOrderMapper.selectById(tradeOrderItemMapper.selectById(id).getOrderId());
        Integer orderRefundPrice = order.getRefundPrice() + refundPrice;
        if (isAllOrderItemAfterSaleSuccess(order.getId())) { // 如果都售后成功，则需要取消订单
            tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId())
                    .setRefundStatus(TradeOrderRefundStatusEnum.ALL.getStatus()).setRefundPrice(orderRefundPrice)
                    .setCancelType(TradeOrderCancelTypeEnum.AFTER_SALE_CLOSE.getType()).setCancelTime(LocalDateTime.now()));

            // TODO 芋艿：记录订单日志

            // TODO 芋艿：站内信？
        } else { // 如果部分售后，则更新退款金额
            tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId())
                    .setRefundStatus(TradeOrderRefundStatusEnum.PART.getStatus()).setRefundPrice(orderRefundPrice));
        }

        // TODO 芋艿：未来如果有分佣，需要更新相关分佣订单为已失效
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderItemComment(Long userId, AppTradeOrderItemCommentCreateReqVO createReqVO) {
        // 先通过订单项 ID，查询订单项是否存在
        TradeOrderItemDO orderItem = tradeOrderItemMapper.selectByIdAndUserId(createReqVO.getOrderItemId(), userId);
        if (orderItem == null) {
            throw exception(ORDER_ITEM_NOT_FOUND);
        }
        // 校验订单相关状态
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(orderItem.getOrderId(), userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(order.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus())) {
            throw exception(ORDER_COMMENT_FAIL_STATUS_NOT_COMPLETED);
        }
        if (ObjectUtil.notEqual(order.getCommentStatus(), Boolean.FALSE)) {
            throw exception(ORDER_COMMENT_STATUS_NOT_FALSE);
        }

        // 1. 创建评价
        ProductCommentCreateReqDTO productCommentCreateReqDTO = TradeOrderConvert.INSTANCE.convert04(createReqVO, orderItem);
        Long comment = productCommentApi.createComment(productCommentCreateReqDTO);

        // 2. 更新订单项评价状态
        tradeOrderItemMapper.updateById(new TradeOrderItemDO().setId(orderItem.getId()).setCommentStatus(Boolean.TRUE));
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        if (!anyMatch(orderItems, item -> Objects.equals(item.getCommentStatus(), Boolean.FALSE))) {
            tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId()).setCommentStatus(Boolean.TRUE));
        }
        return comment;
    }

    /**
     * 判断指定订单的所有订单项，是不是都售后成功
     *
     * @param id 订单编号
     * @return 是否都售后成功
     */
    private boolean isAllOrderItemAfterSaleSuccess(Long id) {
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(id);
        return orderItems.stream().allMatch(orderItem -> Objects.equals(orderItem.getAfterSaleStatus(),
                TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus()));
    }

}
