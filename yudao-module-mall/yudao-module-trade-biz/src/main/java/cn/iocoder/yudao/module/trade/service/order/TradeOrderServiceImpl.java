package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.market.api.price.PriceApi;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.PayOrderDataCreateReqDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuInfoRespDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
@Service
@RequiredArgsConstructor
public class TradeOrderServiceImpl implements TradeOrderService {

    private final TradeOrderMapper tradeOrderMapper;

    private final TradeOrderItemMapper tradeOrderItemMapper;

    private final PriceApi priceApi;

    private final ProductSkuApi productSkuApi;

    private final ProductSpuApi productSpuApi;

    private final PayOrderApi payOrderApi;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTradeOrder(Long loginUserId, String clientIp, AppTradeOrderCreateReqVO createReqVO) {

        List<Item> items = createReqVO.getItems();
        //  商品SKU检查 sku可售状态,库存
        List<SkuInfoRespDTO> skuInfos = productSkuApi.getSkusByIds(CollectionUtils.convertSet(items, Item::getSkuId));
        Map<Long, SkuInfoRespDTO> skuInfoMap = CollectionUtils.convertMap(skuInfos, SkuInfoRespDTO::getId);
        checkSaleableAndStockFromSpu(skuInfoMap, items);

        //  商品SPU检查 sku可售状态,库存
        List<SpuInfoRespDTO> spuInfos = productSpuApi.getSpusByIds(CollectionUtils.convertSet(skuInfos, SkuInfoRespDTO::getSpuId));
        checkSaleableFromSpu(spuInfos);

        // 价格计算
        PriceCalculateReqDTO priceCalculateReqDTO = PriceConvert.INSTANCE.convert(createReqVO, loginUserId);
        PriceCalculateRespDTO priceResp = priceApi.calculatePrice(priceCalculateReqDTO);

        // 订单信息记录
        TradeOrderDO tradeOrderDO = TradeOrderConvert.INSTANCE.convert(createReqVO, priceResp.getOrder());
        tradeOrderMapper.insert(tradeOrderDO);

        // 订单项信息记录
        List<TradeOrderItemDO> tradeOrderItems = TradeOrderItemConvert.INSTANCE.convertList(tradeOrderDO, priceResp.getItems());
        //-填充订单项-SKU信息
        fillItemsInfoFromSku(tradeOrderItems, skuInfoMap);
        tradeOrderItemMapper.insertBatch(tradeOrderItems);

        // 库存扣减
        List<SkuDecrementStockBatchReqDTO.Item> skuDecrementStockItems = ProductSkuConvert.INSTANCE.convert(tradeOrderItems);
        productSkuApi.decrementStockBatch(SkuDecrementStockBatchReqDTO.of(skuDecrementStockItems));
        // 生成预支付

        PayOrderDataCreateReqDTO payOrderCreateReqDTO = PayOrderConvert.INSTANCE.convert(tradeOrderDO);
        return payOrderApi.createPayOrder(payOrderCreateReqDTO);
    }

    private void fillItemsInfoFromSku(List<TradeOrderItemDO> tradeOrderItems,
                                      Map<Long, SkuInfoRespDTO> spuInfos) {
        for (TradeOrderItemDO tradeOrderItem : tradeOrderItems) {
            // 填充SKU信息
            SkuInfoRespDTO skuInfoRespDTO = spuInfos.get(tradeOrderItem.getSkuId());
            tradeOrderItem.setSpuId(skuInfoRespDTO.getSpuId());
            tradeOrderItem.setPicUrl(skuInfoRespDTO.getPicUrl());
            tradeOrderItem.setName(skuInfoRespDTO.getName());
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
