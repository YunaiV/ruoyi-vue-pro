package com.somle.esb.converter.oms;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.*;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import com.somle.esb.enums.PlatformEnum;
import com.somle.lazada.model.reps.LazadaOrderItemResp;
import com.somle.lazada.model.reps.LazadaOrderResp;
import com.somle.lazada.model.reps.LazadaProductResp;
import com.somle.lazada.model.reps.LazadaSellerResp;
import com.somle.lazada.service.LazadaClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_FIRST;

@Component
@Slf4j
public class LazadaToOmsConverter {
    @Resource
    OmsShopApi omsShopApi;

    @Resource
    OmsShopProductApi omsShopProductApi;

    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.LAZADA;

    public OmsShopSaveReqDTO toShops(LazadaSellerResp.ResponseData lazadaShop) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));
        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, lazadaShop.getSellerId(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setExternalName(lazadaShop.getName());
        shopDTO.setCode(null);
        shopDTO.setExternalId(lazadaShop.getSellerId());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }

    public List<OmsShopProductSaveReqDTO> toProducts(List<LazadaProductResp.ResponseData.Product> products, LazadaClient lazadaClient) {

        Map<String, OmsShopDTO> omsShopDTOMap = omsShopApi.getByPlatformCode(this.platform.toString()).stream()
            .collect(Collectors.toMap(OmsShopDTO::getExternalId, Function.identity()));

        OmsShopDTO omsShopDTO = omsShopDTOMap.get(lazadaClient.getSeller().getSellerId());

        if (MapUtil.isEmpty(omsShopDTOMap) || ObjectUtil.isEmpty(omsShopDTO)) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }
        List<Long> existShopIds = omsShopDTOMap.values().stream().map(OmsShopDTO::getId).toList();
        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(existShopIds);
        // 使用Map存储已存在的店铺产品信息，key=externalId, value=OmsShopProductDO
        Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getExternalId(), omsShopProductDO -> omsShopProductDO));

        List<OmsShopProductSaveReqDTO> omsShopProductDOs = new ArrayList<>();
        for (LazadaProductResp.ResponseData.Product product : products) {
            List<LazadaProductResp.ResponseData.Product.Sku> skus = product.getSkus();

            for (LazadaProductResp.ResponseData.Product.Sku sku : skus) {
                OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                MapUtils.findAndThen(existShopProductMap, omsShopDTO.getId() + "#" + sku.getSellerSku(),
                    omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
                shopProductDTO.setShopId(omsShopDTO.getId());
                shopProductDTO.setCode(sku.getSellerSku());
                if (ObjectUtil.isNotEmpty(product.getAttributes())) {
                    shopProductDTO.setName(product.getAttributes().getName());
                }
                shopProductDTO.setExternalId(omsShopDTO.getId() + "#" + sku.getSellerSku());
                shopProductDTO.setUrl(sku.getUrl());
                shopProductDTO.setPrice(new BigDecimal(sku.getPrice()));
                shopProductDTO.setSellableQty(sku.getQuantity());
                omsShopProductDOs.add(shopProductDTO);
            }
        }
        return omsShopProductDOs;
    }


    public List<OmsOrderSaveReqDTO> toOrders(List<LazadaOrderResp.ResponseData.Order> orders, LazadaClient lazadaClient) {
        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        Map<String, OmsShopDTO> omsShopDTOMap = omsShopApi.getByPlatformCode(this.platform.toString()).stream()
            .collect(Collectors.toMap(OmsShopDTO::getExternalId, Function.identity()));

        OmsShopDTO omsShopDTO = omsShopDTOMap.get(lazadaClient.getSeller().getSellerId());

        if (MapUtil.isEmpty(omsShopDTOMap) || ObjectUtil.isEmpty(omsShopDTO)) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));


        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDTO.getId()));
        // 使用Map存储已存在的店铺产品信息，key=shopId, value=Map<String, OmsShopProductDTO>,其中key=code即平台sku
        Map<Long, Map<String, OmsShopProductDTO>> existShopProductMap = existShopProducts.stream()
            .collect(Collectors.groupingBy(
                OmsShopProductDTO::getShopId,  // 外层Map的key: shopId
                Collectors.toMap(
                    OmsShopProductDTO::getCode,  // 内层Map的key: code
                    product -> product,          // 内层Map的value: 产品对象本身
                    (existing, replacement) -> existing  // 如果code重复，保留已存在的
                )
            ));

        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();
        for (LazadaOrderResp.ResponseData.Order order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getOrderId(), omsOrderDTO -> {
                omsOrderSaveReqDTO.setId(omsOrderDTO.getId());
                omsOrderSaveReqDTO.setCode(omsOrderDTO.getCode());
            });
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getOrderId());
            omsOrderSaveReqDTO.setShopId(omsShopDTO.getId());
            omsOrderSaveReqDTO.setPayTime(parseTime(order.getCreatedAt()));
            omsOrderSaveReqDTO.setOrderCreateTime(parseTime(order.getCreatedAt()));
            omsOrderSaveReqDTO.setTotalPrice(new BigDecimal(order.getPrice()));
            omsOrderSaveReqDTO.setBuyerName(order.getCustomerFirstName());

            LazadaOrderResp.ResponseData.Order.Address shippingAddress = order.getAddressShipping();
            omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(shippingAddress));
            if (shippingAddress != null) {
                omsOrderSaveReqDTO.setAddress1(shippingAddress.getAddress1());
                omsOrderSaveReqDTO.setAddress2(shippingAddress.getAddress2());
                omsOrderSaveReqDTO.setAddress3(shippingAddress.getAddress3());
                omsOrderSaveReqDTO.setCity(shippingAddress.getCity());
                omsOrderSaveReqDTO.setPostalCode(shippingAddress.getPostCode());
                omsOrderSaveReqDTO.setRecipientName(shippingAddress.getFirstName() + " " + shippingAddress.getLastName());
                if ("Singapore".equals(shippingAddress.getCountry())) {
                    omsOrderSaveReqDTO.setRecipientCountryCode("SG");
                }
            }

            List<LazadaOrderItemResp.OrderData.OrderItem> orderItems = order.getOrderItems();
            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            //没有字段展示购买数量；需要自己count，一个itemid 就是一个sku，相同的sku也是以不同的item id展示的。
            //用于sku去重
            HashSet<String> uniqueSkuSet = new HashSet<>();
            //用于存储计算购买的单个sku商品数量,key = sku value = 单个sku购买数量
            HashMap<String, Integer> skuQtyMap = new HashMap<>();
            for (LazadaOrderItemResp.OrderData.OrderItem orderItem : orderItems) {
                if (uniqueSkuSet.contains(orderItem.getSku())) {
                    skuQtyMap.put(orderItem.getSku(), skuQtyMap.getOrDefault(orderItem.getSku(), 0) + 1);
                    continue;
                }
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                omsOrderItemSaveReqDTO.setExternalId(orderItem.getOrderItemId());
                if (ObjectUtil.isNotEmpty(existShopProductMap.get(omsOrderSaveReqDTO.getShopId()))) {
                    omsOrderItemSaveReqDTO.setShopProductId(existShopProductMap.get(omsOrderSaveReqDTO.getShopId()).get(orderItem.getSku()).getId());
                }
                omsOrderItemSaveReqDTO.setQty(skuQtyMap.getOrDefault(orderItem.getSku(), 0) + 1);
                omsOrderItemSaveReqDTO.setPrice(new BigDecimal(orderItem.getItemPrice()));
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }

    public LocalDateTime parseTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xx");
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(time, formatter);
        LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
        return localDateTime;
    }
}
