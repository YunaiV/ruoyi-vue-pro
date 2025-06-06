package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.*;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.esb.enums.PlatformEnum;
import com.somle.shopee.model.ShopeeAccount;
import com.somle.shopee.model.reps.ShopeeItemBaseInfoReps;
import com.somle.shopee.model.reps.ShopeeModelListResp;
import com.somle.shopee.model.reps.ShopeeOrderDetailReps;
import com.somle.shopee.model.reps.ShopeeShopReps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_FIRST;

@Component
@Slf4j
public class ShopeeToOmsConverter {


    @Resource
    OmsShopApi omsShopApi;

    @Resource
    OmsShopProductApi omsShopProductApi;

    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.SHOPEE;

    public List<OmsShopSaveReqDTO> toShops(List<ShopeeShopReps> shopeeShopReps, ShopeeAccount shopeeAccount) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        List<OmsShopSaveReqDTO> omsShopSaveReqDTOs = shopeeShopReps.stream().map(shopInfoDTO -> {
            OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
            MapUtils.findAndThen(existShopMap, shopeeAccount.getShopId().toString(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
            shopDTO.setName(null);
            shopDTO.setExternalName(shopInfoDTO.getShopName());
            shopDTO.setCode(null);
            shopDTO.setExternalId(shopeeAccount.getShopId().toString());
            shopDTO.setPlatformCode(this.platform.toString());
            shopDTO.setType(ShopTypeEnum.ONLINE.getType());
            return shopDTO;
        }).toList();
        return omsShopSaveReqDTOs;
    }

    public List<OmsShopProductSaveReqDTO> toProducts(List<ShopeeItemBaseInfoReps.Response.Item> products, ShopeeAccount shopeeAccount) {

        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(shopeeAccount.getShopId().toString());
        if (omsShopDTO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }
        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDTO.getId()));
        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getExternalId(), omsShopProductDO -> omsShopProductDO));

        List<OmsShopProductSaveReqDTO> omsShopProductDOs = new ArrayList<>();
        for (ShopeeItemBaseInfoReps.Response.Item product : products) {
            ShopeeModelListResp shopeeModelListResp = product.getShopeeModelListResp();
            List<ShopeeModelListResp.Model> models = shopeeModelListResp.getResponse().getModel();
            List<ShopeeModelListResp.StandardiseTierVariation> standardiseTierVariation = shopeeModelListResp.getResponse().getStandardiseTierVariation();
            for (int i = 0; i < models.size(); i++) {
                ShopeeModelListResp.Model model = models.get(i);
                OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                MapUtils.findAndThen(existShopProductMap, model.getModelSku() + "#" + omsShopDTO.getId(), omsShopProductDTO -> shopProductDTO.setId(omsShopProductDTO.getId()));
                shopProductDTO.setShopId(omsShopDTO.getId());
                shopProductDTO.setCode(model.getModelSku());
                shopProductDTO.setExternalId(model.getModelSku() + "#" + omsShopDTO.getId());
                shopProductDTO.setName(product.getItemName() + " " + standardiseTierVariation.get(0).getVariationOptionList().get(i).getVariationOptionName());
                if (ObjectUtil.isNotEmpty(model.getPriceInfo())) {
                    shopProductDTO.setPrice(new BigDecimal(model.getPriceInfo().get(0).getCurrentPrice()));
                    shopProductDTO.setCurrencyCode(model.getPriceInfo().get(0).getCurrency());
                }
                ShopeeModelListResp.StockInfoV2 stockInfoV2 = model.getStockInfoV2();
                if (ObjectUtil.isNotEmpty(stockInfoV2) && CollectionUtil.isNotEmpty(stockInfoV2.getSellerStock())) {
                    shopProductDTO.setSellableQty(stockInfoV2.getSellerStock().get(0).getStock());
                }
                omsShopProductDOs.add(shopProductDTO);
            }
        }
        return omsShopProductDOs;
    }

    public List<OmsOrderSaveReqDTO> toOrders(List<ShopeeOrderDetailReps.Order> orders, ShopeeAccount shopeeAccount) {

        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(shopeeAccount.getShopId().toString());
        if (omsShopDTO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));

        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDTO.getId()));
        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getExternalId(), omsShopProductDO -> omsShopProductDO));

        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();

        for (ShopeeOrderDetailReps.Order order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getOrderSn(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getOrderSn());
            omsOrderSaveReqDTO.setShopId(omsShopDTO.getId());
            omsOrderSaveReqDTO.setOrderCreateTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(order.getCreateTime()), ZoneId.systemDefault()));
            omsOrderSaveReqDTO.setPayTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(order.getPayTime()), ZoneId.systemDefault()));
            omsOrderSaveReqDTO.setOutboundLatestTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(order.getShipByDate()), ZoneId.systemDefault()));
            if (order.getTotalAmount() != null) {
                omsOrderSaveReqDTO.setTotalPrice(order.getTotalAmount());
            }


            omsOrderSaveReqDTO.setBuyerName(order.getBuyerUsername());
            omsOrderSaveReqDTO.setRecipientCountryCode(order.getRegion());
            ShopeeOrderDetailReps.RecipientAddress recipientAddress = order.getRecipientAddress();
            if (recipientAddress != null) {
                omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(recipientAddress));
                omsOrderSaveReqDTO.setRecipientName(recipientAddress.getName());
                omsOrderSaveReqDTO.setCity(recipientAddress.getCity());
                omsOrderSaveReqDTO.setPostalCode(recipientAddress.getZipcode());
                omsOrderSaveReqDTO.setState(recipientAddress.getState());
                omsOrderSaveReqDTO.setAddress1(recipientAddress.getFullAddress());
                omsOrderSaveReqDTO.setPhone(recipientAddress.getPhone());

            }

            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            if (CollectionUtil.isEmpty(order.getItemList())) {
                continue;
            }
            BigDecimal totalPrice = new BigDecimal(0);
            for (ShopeeOrderDetailReps.Item item : order.getItemList()) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                String modelSku = item.getModelSku();
                OmsShopProductDTO omsShopProductDTO = existShopProductMap.get(modelSku + "#" + omsShopDTO.getId());
                if (ObjectUtil.isNotEmpty(omsShopProductDTO)) {
                    omsOrderItemSaveReqDTO.setShopProductId(omsShopProductDTO.getId());
                }
                totalPrice = totalPrice.add(item.getModelDiscountedPrice());
                omsOrderItemSaveReqDTO.setShopProductExternalCode(modelSku);
                omsOrderItemSaveReqDTO.setExternalId(item.getModelId().toString());
                omsOrderItemSaveReqDTO.setQty(item.getModelQuantityPurchased());
                if (ObjectUtil.isNotEmpty(item.getModelOriginalPrice())) {
                    omsOrderItemSaveReqDTO.setPrice(item.getModelDiscountedPrice());
                }
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }
            omsOrderSaveReqDTO.setTotalPrice(totalPrice);
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }
}
