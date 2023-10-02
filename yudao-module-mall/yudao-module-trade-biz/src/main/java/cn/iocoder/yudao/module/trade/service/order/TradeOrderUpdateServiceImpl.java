package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.member.api.address.AddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.member.api.level.MemberLevelApi;
import cn.iocoder.yudao.module.member.api.point.MemberPointApi;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.product.api.comment.ProductCommentApi;
import cn.iocoder.yudao.module.product.api.comment.dto.ProductCommentCreateReqDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.promotion.api.bargain.BargainRecordApi;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.promotion.api.coupon.CouponApi;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponUseReqDTO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderRemarkReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderUpdateAddressReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderUpdatePriceReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemCommentCreateReqVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.*;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.framework.order.core.annotations.TradeOrderLog;
import cn.iocoder.yudao.module.trade.framework.order.core.utils.TradeOrderLogUtils;
import cn.iocoder.yudao.module.trade.service.brokerage.BrokerageRecordService;
import cn.iocoder.yudao.module.trade.service.brokerage.BrokerageUserService;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageAddReqBO;
import cn.iocoder.yudao.module.trade.service.cart.CartService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.message.TradeMessageService;
import cn.iocoder.yudao.module.trade.service.message.bo.TradeOrderMessageWhenDeliveryOrderReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterPayOrderReqBO;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.price.TradePriceService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculatorHelper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.minusTime;
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
    private TradeNoRedisDAO tradeNoRedisDAO;

    @Resource
    private List<TradeOrderHandler> tradeOrderHandlers;

    @Resource
    private CartService cartService;
    @Resource
    private TradePriceService tradePriceService;
    @Resource
    private DeliveryExpressService deliveryExpressService;
    @Resource
    private TradeMessageService tradeMessageService;
    @Resource
    private BrokerageUserService brokerageUserService;
    @Resource
    private BrokerageRecordService brokerageRecordService;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private AddressApi addressApi;
    @Resource
    private CouponApi couponApi;
    @Resource
    private CombinationRecordApi combinationRecordApi;
    @Resource
    private BargainRecordApi bargainRecordApi;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private MemberPointApi memberPointApi;
    @Resource
    private ProductCommentApi productCommentApi;

    @Resource
    private TradeOrderProperties tradeOrderProperties;

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
        List<CartDO> cartList = cartService.getCartList(userId,
                convertSet(settlementReqVO.getItems(), AppTradeOrderSettlementReqVO.Item::getCartId));

        // 2. 计算价格
        TradePriceCalculateReqBO calculateReqBO = TradeOrderConvert.INSTANCE.convert(userId, settlementReqVO, cartList);
        calculateReqBO.getItems().forEach(item -> Assert.isTrue(item.getSelected(), // 防御性编程，保证都是选中的
                "商品({}) 未设置为选中", item.getSkuId()));
        return tradePriceService.calculatePrice(calculateReqBO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_CREATE)
    public TradeOrderDO createOrder(Long userId, String userIp, AppTradeOrderCreateReqVO createReqVO) {
        // 0. 价格计算
        TradePriceCalculateRespBO calculateRespBO = calculatePrice(userId, createReqVO);

        // 1. 订单创建前的逻辑
        beforeCreateTradeOrder(userId, createReqVO, calculateRespBO);

        // 2.1 插入 TradeOrderDO 订单
        TradeOrderDO order = createTradeOrder(userId, userIp, createReqVO, calculateRespBO);
        // 2.2 插入 TradeOrderItemDO 订单项
        List<TradeOrderItemDO> orderItems = createTradeOrderItems(order, calculateRespBO);

        // 3. 订单创建后的逻辑
        afterCreateTradeOrder(userId, createReqVO, order, orderItems, calculateRespBO);
        return order;
    }

    private TradeOrderDO createTradeOrder(Long userId, String clientIp, AppTradeOrderCreateReqVO createReqVO,
                                          TradePriceCalculateRespBO calculateRespBO) {
        TradeOrderDO order = TradeOrderConvert.INSTANCE.convert(userId, clientIp, createReqVO, calculateRespBO);
        order.setType(calculateRespBO.getType());
        order.setNo(tradeNoRedisDAO.generate(TradeNoRedisDAO.TRADE_ORDER_NO_PREFIX));
        order.setStatus(TradeOrderStatusEnum.UNPAID.getStatus());
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
        order.setProductCount(getSumValue(calculateRespBO.getItems(), TradePriceCalculateRespBO.OrderItem::getCount, Integer::sum));
        order.setTerminal(TerminalEnum.H5.getTerminal()); // todo 数据来源?
        // 支付 + 退款信息
        order.setAdjustPrice(0).setPayStatus(false);
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus()).setRefundPrice(0);
        // 物流信息
        order.setDeliveryType(createReqVO.getDeliveryType());
        if (Objects.equals(createReqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            AddressRespDTO address = addressApi.getAddress(createReqVO.getAddressId(), userId);
            Assert.notNull(address, "地址({}) 不能为空", createReqVO.getAddressId()); // 价格计算时，已经计算
            order.setReceiverName(address.getName()).setReceiverMobile(address.getMobile())
                    .setReceiverAreaId(address.getAreaId()).setReceiverDetailAddress(address.getDetailAddress());
        } else if (Objects.equals(createReqVO.getDeliveryType(), DeliveryTypeEnum.PICK_UP.getType())) {
            order.setReceiverName(createReqVO.getReceiverName()).setReceiverMobile(createReqVO.getReceiverMobile());
            order.setPickUpVerifyCode(RandomUtil.randomNumbers(8)); // 随机一个核销码，长度为 8 位
        }
        // TODO @疯狂：是不是可以在这里设置下推广人哈；
        tradeOrderMapper.insert(order);
        return order;
    }

    private List<TradeOrderItemDO> createTradeOrderItems(TradeOrderDO tradeOrderDO,
                                                         TradePriceCalculateRespBO calculateRespBO) {
        List<TradeOrderItemDO> orderItems = TradeOrderConvert.INSTANCE.convertList(tradeOrderDO, calculateRespBO);
        tradeOrderItemMapper.insertBatch(orderItems);
        return orderItems;
    }

    /**
     * 订单创建前，执行前置逻辑
     *
     * @param userId          用户编号
     * @param createReqVO     创建订单请求
     * @param calculateRespBO 订单价格计算结果
     */
    private void beforeCreateTradeOrder(Long userId, AppTradeOrderCreateReqVO createReqVO,
                                        TradePriceCalculateRespBO calculateRespBO) {
        // 1. 执行订单创建前置处理器
        // TODO @puhui999：这里有个纠结点；handler 的定义是只处理指定类型的订单的拓展逻辑；还是通用的 handler，类似可以处理优惠劵等等
        tradeOrderHandlers.forEach(handler ->
                handler.beforeOrderCreate(TradeOrderConvert.INSTANCE.convert(userId, createReqVO, calculateRespBO)));

        // 2. 下单时扣减商品库存
        productSkuApi.updateSkuStock(TradeOrderConvert.INSTANCE.convertNegative(createReqVO.getItems()));
    }

    /**
     * 订单创建后，执行后置逻辑
     * <p>
     * 例如说：优惠劵的扣减、积分的扣减、支付单的创建等等
     *
     * @param userId          用户编号
     * @param createReqVO     创建订单请求
     * @param order           交易订单
     * @param calculateRespBO 订单价格计算结果
     */
    private void afterCreateTradeOrder(Long userId, AppTradeOrderCreateReqVO createReqVO,
                                       TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                       TradePriceCalculateRespBO calculateRespBO) {
        // 1. 执行订单创建后置处理器
        tradeOrderHandlers.forEach(handler -> handler.afterOrderCreate(
                TradeOrderConvert.INSTANCE.convert(userId, createReqVO, order, orderItems)));

        // 2. 有使用优惠券时更新
        // 不在前置扣减的原因，是因为优惠劵要记录使用的订单号
        if (createReqVO.getCouponId() != null) {
            couponApi.useCoupon(new CouponUseReqDTO().setId(createReqVO.getCouponId()).setUserId(userId)
                    .setOrderId(order.getId()));
        }

        // 3. 扣减积分（抵扣）
        // 不在前置扣减的原因，是因为积分扣减时，需要记录关联业务
        reduceUserPoint(order.getUserId(), order.getUsePoint(), MemberPointBizTypeEnum.ORDER_USE, order.getId());

        // 4. 删除购物车商品
        Set<Long> cartIds = convertSet(createReqVO.getItems(), AppTradeOrderSettlementReqVO.Item::getCartId);
        if (CollUtil.isNotEmpty(cartIds)) {
            cartService.deleteCart(userId, cartIds);
        }

        // 5. 生成预支付
        createPayOrder(order, orderItems, calculateRespBO);

        // 6. 插入订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), null, order.getStatus());

        // 7. 设置订单推广人
        BrokerageUserDO brokerageUser = brokerageUserService.getBrokerageUser(order.getUserId());
        if (brokerageUser != null && brokerageUser.getBindUserId() != null) {
            tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId()).setBrokerageUserId(brokerageUser.getBindUserId()));
        }

        // TODO @LeeYan9: 是可以思考下, 订单的营销优惠记录, 应该记录在哪里, 微信讨论起来!
    }

    private void createPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                TradePriceCalculateRespBO calculateRespBO) {
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
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_PAY)
    public void updateOrderPaid(Long id, Long payOrderId) {
        // 1. 校验并获得交易订单（可支付）
        KeyValue<TradeOrderDO, PayOrderRespDTO> orderResult = validateOrderPayable(id, payOrderId);
        TradeOrderDO order = orderResult.getKey();
        PayOrderRespDTO payOrder = orderResult.getValue();

        // 2. 更新 TradeOrderDO 状态为已支付，等待发货
        int updateCount = tradeOrderMapper.updateByIdAndStatus(id, order.getStatus(),
                new TradeOrderDO().setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus()).setPayStatus(true)
                        .setPayTime(LocalDateTime.now()).setPayChannelCode(payOrder.getChannelCode()));
        if (updateCount == 0) {
            throw exception(ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }

        // 3、订单支付成功后
        tradeOrderHandlers.forEach(tradeOrderHandler -> tradeOrderHandler.afterPayOrder(new TradeAfterPayOrderReqBO()
                .setOrderId(order.getId()).setOrderType(order.getType()).setUserId(order.getUserId()).setPayTime(LocalDateTime.now())));

        // 4.1 增加用户积分（赠送）
        addUserPoint(order.getUserId(), order.getGivePoint(), MemberPointBizTypeEnum.ORDER_GIVE, order.getId());
        // 4.2 增加用户经验
        getSelf().addUserExperienceAsync(order.getUserId(), order.getPayPrice(), order.getId());
        // 4.3 增加用户佣金
        getSelf().addBrokerageAsync(order.getUserId(), order.getId());

        // 5. 记录订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.UNDELIVERED.getStatus());
        TradeOrderLogUtils.setUserInfo(order.getUserId(), UserTypeEnum.MEMBER.getValue());
    }

    /**
     * 校验交易订单满足被支付的条件
     * <p>
     * 1. 交易订单未支付
     * 2. 支付单已支付
     *
     * @param id         交易订单编号
     * @param payOrderId 支付订单编号
     * @return 交易订单
     */
    private KeyValue<TradeOrderDO, PayOrderRespDTO> validateOrderPayable(Long id, Long payOrderId) {
        // 校验订单是否存在
        TradeOrderDO order = validateOrderExists(id);
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
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_DELIVERY)
    public void deliveryOrder(TradeOrderDeliveryReqVO deliveryReqVO) {
        // 1.1 校验并获得交易订单（可发货）
        TradeOrderDO order = validateOrderDeliverable(deliveryReqVO.getId());
        // 1.2 校验 deliveryType 是否为快递，是快递才可以发货
        if (ObjectUtil.notEqual(order.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS);
        }

        // 2. 更新订单为已发货
        TradeOrderDO updateOrderObj = new TradeOrderDO();
        // 2.1 快递发货
        DeliveryExpressDO express = null;
        if (ObjectUtil.notEqual(deliveryReqVO.getLogisticsId(), TradeOrderDO.LOGISTICS_ID_NULL)) {
            express = deliveryExpressService.validateDeliveryExpress(deliveryReqVO.getLogisticsId());
            updateOrderObj.setLogisticsId(deliveryReqVO.getLogisticsId()).setLogisticsNo(deliveryReqVO.getLogisticsNo());
        } else {
            // 2.2 无需发货
            updateOrderObj.setLogisticsId(0L).setLogisticsNo("");
        }
        // 执行更新
        updateOrderObj.setStatus(TradeOrderStatusEnum.DELIVERED.getStatus()).setDeliveryTime(LocalDateTime.now());
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), updateOrderObj);
        if (updateCount == 0) {
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }

        // 3. 记录订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.DELIVERED.getStatus(),
                MapUtil.<String, Object>builder().put("expressName", express != null ? express.getName() : "")
                        .put("logisticsNo", express != null ? deliveryReqVO.getLogisticsNo() : "").build());

        // 4. 发送站内信
        tradeMessageService.sendMessageWhenDeliveryOrder(new TradeOrderMessageWhenDeliveryOrderReqBO().setOrderId(order.getId())
                .setUserId(order.getUserId()).setMessage(null));
    }

    /**
     * 校验交易订单满足被发货的条件
     * <p>
     * 1. 交易订单未发货
     *
     * @param id 交易订单编号
     * @return 交易订单
     */
    private TradeOrderDO validateOrderDeliverable(Long id) {
        TradeOrderDO order = validateOrderExists(id);
        // 校验订单是否退款
        if (ObjectUtil.notEqual(TradeOrderRefundStatusEnum.NONE.getStatus(), order.getRefundStatus())) {
            throw exception(ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE);
        }
        // 订单类型：拼团
        if (Objects.equals(TradeOrderTypeEnum.COMBINATION.getType(), order.getType())) {
            // 校验订单拼团是否成功
            if (!combinationRecordApi.isCombinationRecordSuccess(order.getUserId(), order.getId())) {
                throw exception(ORDER_DELIVERY_FAIL_COMBINATION_RECORD_STATUS_NOT_SUCCESS);
            }
        }

        return order;
    }

    @NotNull
    private TradeOrderDO validateOrderExists(Long id) {
        // 校验订单是否存在
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_RECEIVE)
    public void receiveOrderByMember(Long userId, Long id) {
        // 校验并获得交易订单（可收货）
        TradeOrderDO order = validateOrderReceivable(userId, id);

        // 收货订单
        receiveOrder0(order);
    }

    @Override
    public int receiveOrderBySystem() {
        // 1. 查询过期的待支付订单
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getReceiveExpireTime());
        List<TradeOrderDO> orders = tradeOrderMapper.selectListByStatusAndDeliveryTimeLt(
                TradeOrderStatusEnum.DELIVERED.getStatus(), expireTime);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行，逐个取消
        int count = 0;
        for (TradeOrderDO order : orders) {
            try {
                getSelf().receiveOrderBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[receiveOrderBySystem][order({}) 自动收货订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    /**
     * 自动收货单个订单
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.SYSTEM_RECEIVE)
    public void receiveOrderBySystem(TradeOrderDO order) {
        receiveOrder0(order);
    }

    /**
     * 收货订单的核心实现
     *
     * @param order 订单
     */
    private void receiveOrder0(TradeOrderDO order) {
        // 更新 TradeOrderDO 状态为已完成
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(),
                new TradeOrderDO().setStatus(TradeOrderStatusEnum.COMPLETED.getStatus()).setReceiveTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }

        // 插入订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus());
    }

    /**
     * 校验交易订单满足可售货的条件
     * <p>
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_CANCEL)
    public void cancelOrderByMember(Long userId, Long id) {
        // 1.1 校验存在
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 1.2 校验状态
        if (ObjectUtil.notEqual(order.getStatus(), TradeOrderStatusEnum.UNPAID.getStatus())) {
            throw exception(ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID);
        }

        // 2. 取消订单
        cancelOrder0(order, TradeOrderCancelTypeEnum.MEMBER_CANCEL);
    }

    @Override
    public int cancelOrderBySystem() {
        // 1. 查询过期的待支付订单
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getPayExpireTime());
        List<TradeOrderDO> orders = tradeOrderMapper.selectListByStatusAndCreateTimeLt(
                TradeOrderStatusEnum.UNPAID.getStatus(), expireTime);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行，逐个取消
        int count = 0;
        for (TradeOrderDO order : orders) {
            try {
                getSelf().cancelOrderBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[cancelOrderBySystem][order({}) 过期订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    /**
     * 自动取消单个订单
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.SYSTEM_CANCEL)
    public void cancelOrderBySystem(TradeOrderDO order) {
        cancelOrder0(order, TradeOrderCancelTypeEnum.PAY_TIMEOUT);
    }

    /**
     * 取消订单的核心实现
     *
     * @param order      订单
     * @param cancelType 取消类型
     */
    private void cancelOrder0(TradeOrderDO order, TradeOrderCancelTypeEnum cancelType) {
        Long id = order.getId();
        // 1. 更新 TradeOrderDO 状态为已取消
        int updateCount = tradeOrderMapper.updateByIdAndStatus(id, order.getStatus(),
                new TradeOrderDO().setStatus(TradeOrderStatusEnum.CANCELED.getStatus())
                        .setCancelType(cancelType.getType()).setCancelTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID);
        }

        // 2. TODO 活动相关库存回滚需要活动 id，活动 id 怎么获取？app 端能否传过来；回复：从订单里拿呀
        tradeOrderHandlers.forEach(handler -> handler.cancelOrder());

        // 3. 回滚库存
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(id);
        productSkuApi.updateSkuStock(TradeOrderConvert.INSTANCE.convert(orderItems));

        // 4. 回滚优惠券
        if (order.getCouponId() != null && order.getCouponId() > 0) {
            couponApi.returnUsedCoupon(order.getCouponId());
        }

        // 5. 回滚积分（抵扣的）
        addUserPoint(order.getUserId(), order.getUsePoint(), MemberPointBizTypeEnum.ORDER_CANCEL, order.getId());

        // 6. 增加订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.CANCELED.getStatus());
    }

    /**
     * 如果金额全部被退款，则取消订单
     * 如果还有未被退款的金额，则无需取消订单
     *
     * @param order       订单
     * @param refundPrice 退款金额
     */
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_CANCEL_AFTER_SALE)
    public void cancelOrderByAfterSale(TradeOrderDO order, Integer refundPrice) {
        // 1. 更新订单
        if (refundPrice < order.getPayPrice()) {
            return;
        }
        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId())
                .setStatus(TradeOrderStatusEnum.CANCELED.getStatus())
                .setCancelType(TradeOrderCancelTypeEnum.AFTER_SALE_CLOSE.getType()).setCancelTime(LocalDateTime.now()));

        // 2. 退还优惠券
        couponApi.returnUsedCoupon(order.getCouponId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_DELETE)
    public void deleteOrder(Long userId, Long id) {
        // 1.1 校验存在
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 1.2 校验状态
        if (ObjectUtil.notEqual(order.getStatus(), TradeOrderStatusEnum.CANCELED.getStatus())) {
            throw exception(ORDER_DELETE_FAIL_STATUS_NOT_CANCEL);
        }
        // 2. 删除订单
        tradeOrderMapper.deleteById(id);

        // 3. 记录日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
    }

    @Override
    public void updateOrderRemark(TradeOrderRemarkReqVO reqVO) {
        // 校验并获得交易订单
        validateOrderExists(reqVO.getId());

        // 更新
        TradeOrderDO order = TradeOrderConvert.INSTANCE.convert(reqVO);
        tradeOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_UPDATE_PRICE)
    public void updateOrderPrice(TradeOrderUpdatePriceReqVO reqVO) {
        // 1.1 校验交易订单
        TradeOrderDO order = validateOrderExists(reqVO.getId());
        if (order.getPayStatus()) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_PAID);
        }
        // 1.2 校验调价金额是否变化
        if (order.getAdjustPrice() > 0) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_ALREADY);
        }
        // 1.3 支付价格不能为 0
        int newPayPrice = order.getPayPrice() + order.getAdjustPrice();
        if (newPayPrice <= 0) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_PRICE_ERROR);
        }

        // 2. 更新订单
        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId())
                .setAdjustPrice(reqVO.getAdjustPrice()).setPayPrice(newPayPrice));

        // 3. 更新 TradeOrderItem，需要做 adjustPrice 的分摊
        List<TradeOrderItemDO> orderOrderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        List<Integer> dividePrices = TradePriceCalculatorHelper.dividePrice2(orderOrderItems, newPayPrice);
        List<TradeOrderItemDO> updateItems = new ArrayList<>();
        for (int i = 0; i < orderOrderItems.size(); i++) {
            TradeOrderItemDO item = orderOrderItems.get(i);
            updateItems.add(new TradeOrderItemDO().setId(item.getId()).setAdjustPrice(dividePrices.get(i))
                    .setPayPrice(item.getPayPrice() + dividePrices.get(i)));
        }
        tradeOrderItemMapper.updateBatch(updateItems);

        // 4. 更新支付订单
        payOrderApi.updatePayOrderPrice(order.getPayOrderId(), newPayPrice);

        // 5. 记录订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus(),
                MapUtil.<String, Object>builder().put("oldPayPrice", MoneyUtils.fenToYuanStr(order.getPayPrice()))
                        .put("newPayPrice", MoneyUtils.fenToYuanStr(newPayPrice)).build());
    }

    @Override
    public void updateOrderAddress(TradeOrderUpdateAddressReqVO reqVO) {
        // 校验交易订单
        validateOrderExists(reqVO.getId());
        // TODO @puhui999：是否需要校验订单是否发货
        // TODO 发货后是否支持修改收货地址

        // 更新
        tradeOrderMapper.updateById(TradeOrderConvert.INSTANCE.convert(reqVO));

        // TODO @puhui999：操作日志
    }

    // =================== Order Item ===================

    @Override
    public void updateOrderItemWhenAfterSaleCreate(Long id, Long afterSaleId) {
        // 更新订单项
        updateOrderItemAfterSaleStatus(id, TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(),
                TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(), afterSaleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderItemWhenAfterSaleSuccess(Long id, Integer refundPrice) {
        // 1. 更新订单项
        updateOrderItemAfterSaleStatus(id, TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
                TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus(), null);

        // 2.1 更新订单的退款金额、积分
        TradeOrderItemDO orderItem = tradeOrderItemMapper.selectById(id);
        TradeOrderDO order = tradeOrderMapper.selectById(orderItem.getOrderId());
        Integer orderRefundPrice = order.getRefundPrice() + refundPrice;
        Integer orderRefundPoint = order.getRefundPoint() + orderItem.getUsePoint();
        Integer refundStatus = isAllOrderItemAfterSaleSuccess(order.getId()) ?
                TradeOrderRefundStatusEnum.ALL.getStatus() // 如果都售后成功，则需要取消订单
                : TradeOrderRefundStatusEnum.PART.getStatus();
        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId())
                .setRefundStatus(refundStatus)
                .setRefundPrice(orderRefundPrice).setRefundPoint(orderRefundPoint));
        // 2.2 如果全部退款，则进行取消订单
        getSelf().cancelOrderByAfterSale(order, orderRefundPrice);

        // TODO @puhui999：活动相关的回滚

        // 3. 回滚库存
        productSkuApi.updateSkuStock(TradeOrderConvert.INSTANCE.convert(Collections.singletonList(orderItem)));

        // 4.1 回滚积分：扣减用户积分（赠送的）
        reduceUserPoint(order.getUserId(), orderItem.getGivePoint(), MemberPointBizTypeEnum.AFTER_SALE_DEDUCT_GIVE, orderItem.getAfterSaleId());
        // 4.2 回滚积分：增加用户积分（返还抵扣）
        addUserPoint(order.getUserId(), orderItem.getUsePoint(), MemberPointBizTypeEnum.AFTER_SALE_REFUND_USED, orderItem.getAfterSaleId());

        // 5. 回滚经验：扣减用户经验
        getSelf().reduceUserExperienceAsync(order.getUserId(), refundPrice, orderItem.getAfterSaleId());

        // 6. 回滚佣金：更新分佣记录为已失效
        getSelf().cancelBrokerageAsync(order.getUserId(), id);
    }

    @Override
    public void updateOrderItemWhenAfterSaleCancel(Long id) {
        // 更新订单项
        updateOrderItemAfterSaleStatus(id, TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
                TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(), null);
    }

    private void updateOrderItemAfterSaleStatus(Long id, Integer oldAfterSaleStatus, Integer newAfterSaleStatus,
                                                Long afterSaleId) {
        // 更新订单项
        int updateCount = tradeOrderItemMapper.updateAfterSaleStatus(id, oldAfterSaleStatus, newAfterSaleStatus, afterSaleId);
        if (updateCount <= 0) {
            throw exception(ORDER_ITEM_UPDATE_AFTER_SALE_STATUS_FAIL);
        }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_COMMENT)
    public Long createOrderItemCommentByMember(Long userId, AppTradeOrderItemCommentCreateReqVO createReqVO) {
        // 1.1 先通过订单项 ID，查询订单项是否存在
        TradeOrderItemDO orderItem = tradeOrderItemMapper.selectByIdAndUserId(createReqVO.getOrderItemId(), userId);
        if (orderItem == null) {
            throw exception(ORDER_ITEM_NOT_FOUND);
        }
        // 1.2 校验订单相关状态
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

        // 2. 创建评价
        Long commentId = createOrderItemComment0(orderItem, createReqVO);

        // 3. 如果订单项都评论了，则更新订单评价状态
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        if (!anyMatch(orderItems, item -> Objects.equals(item.getCommentStatus(), Boolean.FALSE))) {
            tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId()).setCommentStatus(Boolean.TRUE)
                    .setFinishTime(LocalDateTime.now()));
            // 增加订单日志。注意：只有在所有订单项都评价后，才会增加
            TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
        }
        return commentId;
    }

    @Override
    public int createOrderItemCommentBySystem() {
        // 1. 查询过期的待支付订单
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getCommentExpireTime());
        List<TradeOrderDO> orders = tradeOrderMapper.selectListByStatusAndReceiveTimeLt(
                TradeOrderStatusEnum.COMPLETED.getStatus(), expireTime, false);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行，逐个取消
        int count = 0;
        for (TradeOrderDO order : orders) {
            try {
                getSelf().createOrderItemCommentBySystemBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[createOrderItemCommentBySystem][order({}) 过期订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    /**
     * 创建单个订单的评论
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.SYSTEM_COMMENT)
    public void createOrderItemCommentBySystemBySystem(TradeOrderDO order) {
        // 1. 查询未评论的订单项
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderIdAndCommentStatus(order.getId(), Boolean.FALSE);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }

        // 2. 逐个评论
        for (TradeOrderItemDO orderItem : orderItems) {
            // 2.1 创建评价
            AppTradeOrderItemCommentCreateReqVO commentCreateReqVO = new AppTradeOrderItemCommentCreateReqVO()
                    .setOrderItemId(orderItem.getId()).setAnonymous(false).setContent("")
                    .setBenefitScores(5).setDescriptionScores(5);
            createOrderItemComment0(orderItem, commentCreateReqVO);

            // 2.2 更新订单项评价状态
            tradeOrderItemMapper.updateById(new TradeOrderItemDO().setId(orderItem.getId()).setCommentStatus(Boolean.TRUE));
        }

        // 3. 所有订单项都评论了，则更新订单评价状态
        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId()).setCommentStatus(Boolean.TRUE)
                .setFinishTime(LocalDateTime.now()));
        // 增加订单日志。注意：只有在所有订单项都评价后，才会增加
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
    }

    /**
     * 创建订单项的评论的核心实现
     *
     * @param orderItem   订单项
     * @param createReqVO 评论内容
     * @return 评论编号
     */
    private Long createOrderItemComment0(TradeOrderItemDO orderItem, AppTradeOrderItemCommentCreateReqVO createReqVO) {
        // 1. 创建评价
        ProductCommentCreateReqDTO productCommentCreateReqDTO = TradeOrderConvert.INSTANCE.convert04(createReqVO, orderItem);
        Long commentId = productCommentApi.createComment(productCommentCreateReqDTO);

        // 2. 更新订单项评价状态
        tradeOrderItemMapper.updateById(new TradeOrderItemDO().setId(orderItem.getId()).setCommentStatus(Boolean.TRUE));
        return commentId;
    }

    // =================== 营销相关的操作 ===================

    @Async
    protected void addUserExperienceAsync(Long userId, Integer payPrice, Long orderId) {
        int bizType = MemberExperienceBizTypeEnum.ORDER.getType();
        memberLevelApi.addExperience(userId, payPrice, bizType, String.valueOf(orderId));
    }

    @Async
    protected void reduceUserExperienceAsync(Long userId, Integer refundPrice, Long afterSaleId) {
        int bizType = MemberExperienceBizTypeEnum.REFUND.getType();
        memberLevelApi.addExperience(userId, -refundPrice, bizType, String.valueOf(afterSaleId));
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
    protected void addUserPoint(Long userId, Integer point, MemberPointBizTypeEnum bizType, Long bizId) {
        if (point != null && point > 0) {
            memberPointApi.addPoint(userId, point, bizType.getType(), String.valueOf(bizId));
        }
    }

    protected void reduceUserPoint(Long userId, Integer point, MemberPointBizTypeEnum bizType, Long bizId) {
        if (point != null && point > 0) {
            memberPointApi.reducePoint(userId, point, bizType.getType(), String.valueOf(bizId));
        }
    }

    /**
     * 创建分销记录
     * <p>
     * 目前是支付成功后，就会创建分销记录。
     * <p>
     * 业内还有两种做法，可以根据自己的业务调整：
     * 1. 确认收货后，才创建分销记录
     * 2. 支付 or 下单成功时，创建分销记录（冻结），确认收货解冻或者 n 天后解冻
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     */
    @Async
    protected void addBrokerageAsync(Long userId, Long orderId) {
        MemberUserRespDTO user = memberUserApi.getUser(userId);
        Assert.notNull(user);
        // 每一个订单项，都会去生成分销记录
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(orderId);
        List<BrokerageAddReqBO> addList = convertList(orderItems,
                item -> TradeOrderConvert.INSTANCE.convert(user, item,
                        productSpuApi.getSpu(item.getSpuId()), productSkuApi.getSku(item.getSkuId())));
        brokerageRecordService.addBrokerage(userId, BrokerageRecordBizTypeEnum.ORDER, addList);
    }

    @Async
    protected void cancelBrokerageAsync(Long userId, Long orderItemId) {
        brokerageRecordService.cancelBrokerage(userId, BrokerageRecordBizTypeEnum.ORDER, String.valueOf(orderItemId));
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private TradeOrderUpdateServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
