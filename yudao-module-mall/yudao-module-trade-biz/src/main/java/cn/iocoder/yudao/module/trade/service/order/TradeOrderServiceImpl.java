package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.promotion.api.price.PriceApi;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.PayOrderInfoCreateReqDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.SpuInfoRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO.Item;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderItemConvert;
import cn.iocoder.yudao.module.trade.convert.pay.PayOrderConvert;
import cn.iocoder.yudao.module.trade.convert.price.PriceConvert;
import cn.iocoder.yudao.module.trade.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.orderitem.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemRefundStatusEnum;
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

/**
 * TODO @LeeYan9: 注释
 * @author LeeYan9
 * @since 2022-08-26
 */
@Service
public class TradeOrderServiceImpl implements TradeOrderService {

    // TODO @LeeYan9: 相同类型的, 可以放在一起,不用空行; 例如说 Mapper 和 API 和 Properties

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
    private TradeOrderProperties tradeOrderProperties;

    // TODO LeeYan9: 静态变量, 需要在最前面哈; 另外, 静态变量的注释最好写下;
    private static final String BLANK_PLACEHOLDER = " ";
    private static final String MULTIPLIER_PLACEHOLDER = "x";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTradeOrder(Long loginUserId, String clientIp, AppTradeOrderCreateReqVO createReqVO) {

        List<Item> items = createReqVO.getItems(); // TODO @LeeYan9: 方法第一行, 不用空哈;
        // 商品SKU检查 sku可售状态,库存
        List<ProductSkuRespDTO> skuInfos = productSkuApi.getSkuList(CollectionUtils.convertSet(items, Item::getSkuId));
        Map<Long, ProductSkuRespDTO> skuInfoMap = CollectionUtils.convertMap(skuInfos, ProductSkuRespDTO::getId);
        checkSaleableAndStockFromSpu(skuInfoMap, items);

        // 商品SPU检查 sku可售状态,库存
        List<SpuInfoRespDTO> spuInfos = productSpuApi.getSpuList(CollectionUtils.convertSet(skuInfos, ProductSkuRespDTO::getSpuId));
        checkSaleableFromSpu(spuInfos);

        // 价格计算
        PriceCalculateReqDTO priceCalculateReqDTO = PriceConvert.INSTANCE.convert(createReqVO, loginUserId);
        PriceCalculateRespDTO priceResp = priceApi.calculatePrice(priceCalculateReqDTO);
        // TODO @LeeYan9: 是可以思考下, 订单的营销优惠记录, 应该记录在哪里, 微信讨论起来!

        // 订单信息记录
        TradeOrderDO tradeOrderDO = TradeOrderConvert.INSTANCE.convert(createReqVO, priceResp.getOrder());
        fillTradeOrderInfoFromReqInfo(tradeOrderDO,createReqVO,loginUserId, clientIp); // TODO @LeeYan9: tradeOrderDO, createReqVO, loginUserId, clientIp
        tradeOrderMapper.insert(tradeOrderDO);

        // 订单项信息记录
        List<TradeOrderItemDO> tradeOrderItems = TradeOrderItemConvert.INSTANCE.convertList(priceResp.getOrder().getItems());
        // 填充订单项-SKU信息
        fillItemsInfoFromSkuAndOrder(tradeOrderDO, tradeOrderItems, skuInfoMap);
        tradeOrderItemMapper.insertBatch(tradeOrderItems);

        // TODO @LeeYan9: 先扣减库存哈; 可能会扣减失败; 毕竟 get 和 update 之间, 会有并发的可能性
        // 库存扣减
        List<SkuDecrementStockBatchReqDTO.Item> skuDecrementStockItems = ProductSkuConvert.INSTANCE.convert(tradeOrderItems);
        productSkuApi.decrementStockBatch(SkuDecrementStockBatchReqDTO.of(skuDecrementStockItems));

        // 构建预支付请求参数
        // TODO @LeeYan9: 需要更新到订单上
        PayOrderInfoCreateReqDTO payOrderCreateReqDTO = PayOrderConvert.INSTANCE.convert(tradeOrderDO);
        fillPayOrderInfoFromItems(payOrderCreateReqDTO, tradeOrderItems);
        // 生成预支付
        return payOrderApi.createPayOrder(payOrderCreateReqDTO);
    }

    // TODO @LeeYan9: 填充就好, 不用 from 哈;
    private void fillTradeOrderInfoFromReqInfo(TradeOrderDO tradeOrderDO, AppTradeOrderCreateReqVO createReqVO,
                                               Long loginUserId, String clientIp) {
        tradeOrderDO.setUserId(loginUserId);
        tradeOrderDO.setUserIp(clientIp);
        tradeOrderDO.setNo(IdUtil.getSnowflakeNextId() + ""); // TODO @LeeYan9: 思考下, 怎么生成好点哈; 这个是会展示给用户的;
        tradeOrderDO.setStatus(TradeOrderStatusEnum.WAITING_PAYMENT.getStatus());
        tradeOrderDO.setType(TradeOrderTypeEnum.NORMAL.getType());
        tradeOrderDO.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
        tradeOrderDO.setProductCount(CollectionUtils.getSumValue(createReqVO.getItems(), Item::getCount,Integer::sum));
        // todo 地址&用户信息解析

        // todo 数据来源?
        tradeOrderDO.setTerminal(TerminalEnum.H5.getTerminal());
    }

    private void fillPayOrderInfoFromItems(PayOrderInfoCreateReqDTO payOrderInfoCreateReqDTO,
                                           List<TradeOrderItemDO> tradeOrderItems) {
        // 填写 商品&应用信息
        payOrderInfoCreateReqDTO.setMerchantOrderId(tradeOrderProperties.getMerchantOrderId());
        payOrderInfoCreateReqDTO.setAppId(tradeOrderProperties.getAppId());

        // 填写商品信息
        StrBuilder subject = new StrBuilder();
        StrBuilder body = new StrBuilder();
        for (TradeOrderItemDO tradeOrderItem : tradeOrderItems) {
            // append subject
            subject.append(BLANK_PLACEHOLDER);
            subject.append(tradeOrderItem.getName());
            // append body
            body.append(BLANK_PLACEHOLDER);
            body.append(tradeOrderItem.getName());
            body.append(MULTIPLIER_PLACEHOLDER);
            body.append(tradeOrderItem.getCount());
        }
        // 设置 subject & body
        // TODO @LeeYan9: 可以抽象一个 StrUtils 方法; 或者看看 hutool 有没自带的哈
        payOrderInfoCreateReqDTO.setSubject(StrUtils.maxLength(subject.subString(1), 32));
        payOrderInfoCreateReqDTO.setBody(StrUtils.maxLength(body.subString(1), 128));
    }

    private void fillItemsInfoFromSkuAndOrder(TradeOrderDO tradeOrderDO, List<TradeOrderItemDO> tradeOrderItems,
                                              Map<Long, ProductSkuRespDTO> spuInfos) {
        for (TradeOrderItemDO tradeOrderItem : tradeOrderItems) {
            // 填充订单信息
            tradeOrderItem.setOrderId(tradeOrderDO.getId());
            tradeOrderItem.setUserId(tradeOrderDO.getUserId());
            // 填充SKU信息
            ProductSkuRespDTO skuInfoRespDTO = spuInfos.get(tradeOrderItem.getSkuId());
            tradeOrderItem.setSpuId(skuInfoRespDTO.getSpuId());
            tradeOrderItem.setPicUrl(skuInfoRespDTO.getPicUrl());
            tradeOrderItem.setName(skuInfoRespDTO.getName());
            tradeOrderItem.setRefundStatus(TradeOrderItemRefundStatusEnum.NONE.getStatus());
            // todo
            List<TradeOrderItemDO.Property> property =
                    BeanUtil.copyToList(skuInfoRespDTO.getProperties(), TradeOrderItemDO.Property.class);
            tradeOrderItem.setProperties(property);
        }
    }

    private void checkSaleableFromSpu(List<SpuInfoRespDTO> spuInfos) {
        SpuInfoRespDTO spu = CollectionUtils.findFirst(spuInfos,
                spuInfoDTO -> !Objects.equals(ProductSpuStatusEnum.ENABLE.getStatus(), spuInfoDTO.getStatus()));
        if (Objects.isNull(spu)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.ORDER_SPU_NOT_SALE);
        }
    }

    // TODO @LeeYan9: checkSpuXXX 会不会好点哈? ps: 这个貌似是 sku 哈
    private void checkSaleableAndStockFromSpu(Map<Long, ProductSkuRespDTO> skuInfoMap,
                                              List<Item> items) {
        // sku 不存在
        if (items.size() != skuInfoMap.size()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.ORDER_SKU_NOT_FOUND);
        }
        for (Item item : items) {
            ProductSkuRespDTO skuInfoDTO = skuInfoMap.get(item.getSkuId());
            // sku禁用
            if (!Objects.equals(CommonStatusEnum.ENABLE.getStatus(), skuInfoDTO.getStatus())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.ORDER_SKU_NOT_SALE);
            }
            // sku库存不足
            if (item.getCount() > skuInfoDTO.getStock()) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.ORDER_SKU_STOCK_NOT_ENOUGH);
            }
        }

    }
}
