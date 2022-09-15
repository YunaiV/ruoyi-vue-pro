package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.market.api.price.PriceApi;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.PayOrderInfoCreateReqDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
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
import cn.iocoder.yudao.module.trade.enums.enums.ErrorCodeConstants;
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
    private TradeOrderProperties tradeOrderProperties;

    private static final String BLANK_PLACEHOLDER = " ";
    private static final String MULTIPLIER_PLACEHOLDER = "x";


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTradeOrder(Long loginUserId, String clientIp, AppTradeOrderCreateReqVO createReqVO) {

        List<Item> items = createReqVO.getItems();
        // 商品SKU检查 sku可售状态,库存
        List<SkuInfoRespDTO> skuInfos = productSkuApi.getSkusByIds(CollectionUtils.convertSet(items, Item::getSkuId));
        Map<Long, SkuInfoRespDTO> skuInfoMap = CollectionUtils.convertMap(skuInfos, SkuInfoRespDTO::getId);
        checkSaleableAndStockFromSpu(skuInfoMap, items);

        // 商品SPU检查 sku可售状态,库存
        List<SpuInfoRespDTO> spuInfos = productSpuApi.getSpusByIds(CollectionUtils.convertSet(skuInfos, SkuInfoRespDTO::getSpuId));
        checkSaleableFromSpu(spuInfos);

        // 价格计算
        PriceCalculateReqDTO priceCalculateReqDTO = PriceConvert.INSTANCE.convert(createReqVO, loginUserId);
        PriceCalculateRespDTO priceResp = priceApi.calculatePrice(priceCalculateReqDTO);
        // 订单信息记录
        TradeOrderDO tradeOrderDO = TradeOrderConvert.INSTANCE.convert(createReqVO, priceResp.getOrder());
        fillTradeOrderInfoFromReqInfo(tradeOrderDO,createReqVO,loginUserId, clientIp);
        tradeOrderMapper.insert(tradeOrderDO);

        // 订单项信息记录
        List<TradeOrderItemDO> tradeOrderItems = TradeOrderItemConvert.INSTANCE.convertList(priceResp.getItems());
        //-填充订单项-SKU信息
        fillItemsInfoFromSkuAndOrder(tradeOrderDO, tradeOrderItems, skuInfoMap);
        tradeOrderItemMapper.insertBatch(tradeOrderItems);

        // 库存扣减
        List<SkuDecrementStockBatchReqDTO.Item> skuDecrementStockItems = ProductSkuConvert.INSTANCE.convert(tradeOrderItems);
        productSkuApi.decrementStockBatch(SkuDecrementStockBatchReqDTO.of(skuDecrementStockItems));

        // 构建预支付请求参数
        PayOrderInfoCreateReqDTO payOrderCreateReqDTO = PayOrderConvert.INSTANCE.convert(tradeOrderDO);
        fillPayOrderInfoFromItems(payOrderCreateReqDTO, tradeOrderItems);
        // 生成预支付
        return payOrderApi.createPayOrder(payOrderCreateReqDTO);
    }

    private void fillTradeOrderInfoFromReqInfo(TradeOrderDO tradeOrderDO, AppTradeOrderCreateReqVO createReqVO,
                                               Long loginUserId, String clientIp) {
        tradeOrderDO.setUserId(loginUserId);
        tradeOrderDO.setUserIp(clientIp);
        tradeOrderDO.setSn(IdUtil.getSnowflakeNextId() + "");
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
        payOrderInfoCreateReqDTO.setSubject(StrUtils.maxLength(subject.subString(1), 32));
        payOrderInfoCreateReqDTO.setBody(StrUtils.maxLength(body.subString(1), 128));
    }

    private void fillItemsInfoFromSkuAndOrder(TradeOrderDO tradeOrderDO, List<TradeOrderItemDO> tradeOrderItems,
                                              Map<Long, SkuInfoRespDTO> spuInfos) {
        for (TradeOrderItemDO tradeOrderItem : tradeOrderItems) {
            // 填充订单信息
            tradeOrderItem.setOrderId(tradeOrderDO.getId());
            tradeOrderItem.setUserId(tradeOrderDO.getUserId());
            // 填充SKU信息
            SkuInfoRespDTO skuInfoRespDTO = spuInfos.get(tradeOrderItem.getSkuId());
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
                spuInfoDTO -> !Objects.equals(ProductSpuStatusEnum.ENABLE.getStyle(), spuInfoDTO.getStatus()));
        if (Objects.isNull(spu)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.ORDER_SPU_NOT_SALE);
        }
    }

    private void checkSaleableAndStockFromSpu(Map<Long, SkuInfoRespDTO> skuInfoMap,
                                              List<Item> items) {
        // sku 不存在
        if (items.size() != skuInfoMap.size()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.ORDER_SKU_NOT_FOUND);
        }
        for (Item item : items) {
            SkuInfoRespDTO skuInfoDTO = skuInfoMap.get(item.getSkuId());
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
