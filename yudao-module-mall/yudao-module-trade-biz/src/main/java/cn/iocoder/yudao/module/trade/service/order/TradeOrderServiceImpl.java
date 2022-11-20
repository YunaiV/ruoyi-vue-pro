package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.member.api.address.AddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.PayOrderInfoCreateReqDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.promotion.api.coupon.CouponApi;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponUseReqDTO;
import cn.iocoder.yudao.module.promotion.api.price.PriceApi;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO.Item;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.orderitem.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_CREATE_SKU_NOT_SALE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_CREATE_SPU_NOT_FOUND;

/**
 * 交易订单 Service 实现类
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Service
public class TradeOrderServiceImpl implements TradeOrderService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;

    @Resource
    private PriceApi priceApi;
    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private AddressApi addressApi;
    @Resource
    private CouponApi couponApi;

    @Resource
    private TradeOrderProperties tradeOrderProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTradeOrder(Long userId, String userIp, AppTradeOrderCreateReqVO createReqVO) {
        // 商品 SKU 检查：可售状态、库存
        List<ProductSkuRespDTO> skus = validateSkuSaleable(createReqVO.getItems());
        // 商品 SPU 检查：可售状态
        List<ProductSpuRespDTO> spus = validateSpuSaleable(convertSet(skus, ProductSkuRespDTO::getSpuId));
        // 用户收件地址的校验
        AddressRespDTO address = validateAddress(userId, createReqVO.getAddressId());

        // 价格计算
        PriceCalculateRespDTO priceResp = priceApi.calculatePrice(TradeOrderConvert.INSTANCE.convert(createReqVO, userId));

        // 插入 TradeOrderDO 订单
        TradeOrderDO tradeOrderDO = createTradeOrder(userId, userIp, createReqVO, priceResp.getOrder(), address);
        // 插入 TradeOrderItemDO 订单项
        List<TradeOrderItemDO> tradeOrderItems = createTradeOrderItems(tradeOrderDO, priceResp.getOrder().getItems(), skus);

        // 订单创建完后的逻辑
        afterCreateTradeOrder(userId, createReqVO, tradeOrderDO, tradeOrderItems, spus);
        // TODO @LeeYan9: 是可以思考下, 订单的营销优惠记录, 应该记录在哪里, 微信讨论起来!
        return tradeOrderDO.getId();
    }

    /**
     * 校验商品 SKU 是否可出售
     *
     * @param items 商品 SKU
     * @return 商品 SKU 数组
     */
    private List<ProductSkuRespDTO> validateSkuSaleable(List<Item> items) {
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(convertSet(items, Item::getSkuId));
        // SKU 不存在
        if (items.size() != skus.size()) {
            throw exception(ErrorCodeConstants.ORDER_CREATE_SKU_NOT_FOUND);
        }
        // 校验是否禁用 or 库存不足
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        items.forEach(item -> {
            ProductSkuRespDTO sku = skuMap.get(item.getSkuId());
            // SKU 禁用
            if (ObjectUtil.notEqual(CommonStatusEnum.ENABLE.getStatus(), sku.getStatus())) {
                throw exception(ORDER_CREATE_SKU_NOT_SALE);
            }
            // SKU 库存不足
            if (item.getCount() > sku.getStock()) {
                throw exception(ErrorCodeConstants.ORDER_CREATE_SKU_STOCK_NOT_ENOUGH);
            }
        });
        return skus;
    }

    /**
     * 校验商品 SPU 是否可出售
     *
     * @param spuIds 商品 SPU 编号数组
     * @return 商品 SPU 数组
     */
    private List<ProductSpuRespDTO> validateSpuSaleable(Set<Long> spuIds) {
        List<ProductSpuRespDTO> spus = productSpuApi.getSpuList(spuIds);
        // SPU 不存在
        if (spus.size() != spuIds.size()) {
            throw exception(ORDER_CREATE_SPU_NOT_FOUND);
        }
        // 校验是否存在禁用的 SPU
        ProductSpuRespDTO spu = CollectionUtils.findFirst(spus,
                spuDTO -> ObjectUtil.notEqual(ProductSpuStatusEnum.ENABLE.getStatus(), spuDTO.getStatus()));
        if (spu != null) {
            throw exception(ErrorCodeConstants.ORDER_CREATE_SPU_NOT_SALE);
        }
        return spus;
    }

    /**
     * 校验收件地址是否存在
     *
     * @param userId 用户编号
     * @param addressId 收件地址编号
     * @return 收件地址
     */
    private AddressRespDTO validateAddress(Long userId, Long addressId) {
        AddressRespDTO address = addressApi.getAddress(addressId, userId);
        if (Objects.isNull(address)) {
            throw exception(ErrorCodeConstants.ORDER_CREATE_ADDRESS_NOT_FOUND);
        }
        return address;
    }

    private TradeOrderDO createTradeOrder(Long userId, String clientIp, AppTradeOrderCreateReqVO createReqVO,
                                          PriceCalculateRespDTO.Order order, AddressRespDTO address) {
        TradeOrderDO tradeOrderDO = TradeOrderConvert.INSTANCE.convert(userId, clientIp, createReqVO, order, address);
        tradeOrderDO.setNo(IdUtil.getSnowflakeNextId() + ""); // TODO @LeeYan9: 思考下, 怎么生成好点哈; 这个是会展示给用户的;
        tradeOrderDO.setStatus(TradeOrderStatusEnum.WAITING_PAYMENT.getStatus());
        tradeOrderDO.setType(TradeOrderTypeEnum.NORMAL.getType());
        tradeOrderDO.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
        tradeOrderDO.setProductCount(getSumValue(order.getItems(),  PriceCalculateRespDTO.OrderItem::getCount, Integer::sum));
        tradeOrderDO.setTerminal(TerminalEnum.H5.getTerminal()); // todo 数据来源?
        tradeOrderDO.setAdjustPrice(0).setPayed(false); // 支付信息
        tradeOrderDO.setDeliveryStatus(false); // 物流信息
        tradeOrderDO.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus()).setRefundPrice(0); // 退款信息
        tradeOrderMapper.insert(tradeOrderDO);
        return tradeOrderDO;
    }

    private List<TradeOrderItemDO> createTradeOrderItems(TradeOrderDO tradeOrderDO,
                                                         List<PriceCalculateRespDTO.OrderItem> orderItems, List<ProductSkuRespDTO> skus) {
        List<TradeOrderItemDO> tradeOrderItemDOs = TradeOrderConvert.INSTANCE.convertList(tradeOrderDO, orderItems, skus);
        tradeOrderItemMapper.insertBatch(tradeOrderItemDOs);
        return tradeOrderItemDOs;
    }

    /**
     * 执行创建完创建完订单后的逻辑
     *
     * 例如说：优惠劵的扣减、积分的扣减、支付单的创建等等
     *
     * @param userId 用户编号
     * @param createReqVO 创建订单请求
     * @param tradeOrderDO 交易订单
     */
    private void afterCreateTradeOrder(Long userId, AppTradeOrderCreateReqVO createReqVO,
                                       TradeOrderDO tradeOrderDO, List<TradeOrderItemDO> tradeOrderItemDOs,
                                       List<ProductSpuRespDTO> spus) {
        // 下单时扣减商品库存
        productSkuApi.updateSkuStock(new ProductSkuUpdateStockReqDTO(TradeOrderConvert.INSTANCE.convertList(tradeOrderItemDOs)));

        // 删除购物车商品 TODO 芋艿：待实现

        // 扣减积分，抵扣金额 TODO 芋艿：待实现

        // 有使用优惠券时更新
        if (createReqVO.getCouponId() != null) {
            couponApi.useCoupon(new CouponUseReqDTO().setId(createReqVO.getCouponId()).setUserId(userId)
                    .setOrderId(tradeOrderDO.getId()));
        }

        // 生成预支付
        createPayOrder(tradeOrderDO, tradeOrderItemDOs, spus);

        // 增加订单日志 TODO 芋艿：待实现
    }

    private void createPayOrder(TradeOrderDO tradeOrderDO, List<TradeOrderItemDO> tradeOrderItemDOs,
                                List<ProductSpuRespDTO> spus) {
        // 创建支付单，用于后续的支付
        PayOrderInfoCreateReqDTO payOrderCreateReqDTO = TradeOrderConvert.INSTANCE.convert(
                tradeOrderDO, tradeOrderItemDOs, spus, tradeOrderProperties);
        Long payOrderId = payOrderApi.createPayOrder(payOrderCreateReqDTO);

        // 更新到交易单上
        tradeOrderMapper.updateById(new TradeOrderDO().setId(tradeOrderDO.getId()).setPayOrderId(payOrderId));
    }

}
