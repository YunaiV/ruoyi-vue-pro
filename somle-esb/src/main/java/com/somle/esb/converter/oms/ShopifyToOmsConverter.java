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
import com.somle.lazada.service.ShopifyClient;
import com.somle.shopify.model.reps.ShopifyOrderRepsVO;
import com.somle.shopify.model.reps.ShopifyShopProductRepsVO;
import com.somle.shopify.model.reps.ShopifyShopRepsVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_FIRST;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_LACK;


@Component
@Slf4j
public class ShopifyToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;
    @Resource
    OmsShopProductApi omsShopProductApi;
    @Resource
    OmsOrderApi omsOrderApi;

    private ShopifyShopRepsVO shopifyShopRepsVO;

    private PlatformEnum platform = PlatformEnum.SHOPIFY;

    public OmsShopSaveReqDTO toShops(ShopifyShopRepsVO shopInfoDTO) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, shopInfoDTO.getId().toString(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setExternalName(shopInfoDTO.getName());
        shopDTO.setCode(null);
        shopDTO.setExternalId(shopInfoDTO.getId().toString());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }

    public List<OmsShopProductSaveReqDTO> toProducts(List<ShopifyShopProductRepsVO> product, ShopifyClient shopifyClient) {
        if (shopifyClient.getShop() == null) {
            throw exception(OMS_SYNC_SHOP_INFO_LACK);
        }

        //根据client再次请求获取店铺信息,shopify一个店铺对应一个client
        if (shopifyShopRepsVO == null) {
            shopifyShopRepsVO = shopifyClient.getShop();
        }


        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(shopifyShopRepsVO.getId().toString());

        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDO.getId()));
        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDTO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getExternalId(), omsShopProductDO -> omsShopProductDO));


        List<OmsShopProductSaveReqDTO> omsShopProductDOs = product.stream()
            .flatMap(shopifyProductRepsDTO ->
                shopifyProductRepsDTO.getVariants().stream()
                    .map(variant -> {
                        OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                        MapUtils.findAndThen(existShopProductMap, variant.getId().toString(), omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
                        shopProductDTO.setShopId(omsShopDO.getId());
                        shopProductDTO.setCode(variant.getSku());
                        shopProductDTO.setExternalId(variant.getId().toString());
                        shopProductDTO.setName(shopifyProductRepsDTO.getTitle() + " " + variant.getTitle());
                        if (variant.getPrice() != null) {
                            shopProductDTO.setPrice(new BigDecimal(variant.getPrice()));
                        }
                        shopProductDTO.setSellableQty(variant.getInventoryQuantity());
                        return shopProductDTO;
                    })
            ).toList();
        return omsShopProductDOs;
    }

    public List<OmsOrderSaveReqDTO> toOrders(List<ShopifyOrderRepsVO> orders, ShopifyClient shopifyClient) {

        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        //根据client再次请求获取店铺信息,shopify一个店铺对应一个client
        if (shopifyShopRepsVO == null) {
            shopifyShopRepsVO = shopifyClient.getShop();
        }

        OmsShopDTO omsShopDO = omsShopApi.getShopByPlatformShopCode(shopifyShopRepsVO.getId().toString());
        if (omsShopDO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));

        List<OmsShopProductDTO> existShopProducts = omsShopProductApi.getByShopIds(List.of(omsShopDO.getId()));
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

        for (ShopifyOrderRepsVO order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getId().toString() + order.getName(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getId().toString() + order.getName());
            omsOrderSaveReqDTO.setShopId(omsShopDO.getId());
            omsOrderSaveReqDTO.setTotalPrice(new BigDecimal(order.getTotalPrice()));
            omsOrderSaveReqDTO.setBuyerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
            omsOrderSaveReqDTO.setPhone(order.getCustomer().getPhone());
            omsOrderSaveReqDTO.setEmail(order.getCustomer().getEmail());
            omsOrderSaveReqDTO.setOrderCreateTime(parseTime(order.getCreatedAt()));
            omsOrderSaveReqDTO.setPayTime(parseTime(order.getCreatedAt()));

            ShopifyOrderRepsVO.ShippingAddressDTO shippingAddress = order.getShippingAddress();
            if (ObjectUtil.isNotEmpty(shippingAddress)) {
                omsOrderSaveReqDTO.setRecipientCountryCode(shippingAddress.getCountryCode());
                omsOrderSaveReqDTO.setState(shippingAddress.getProvinceCode());
                omsOrderSaveReqDTO.setCity(shippingAddress.getCity());
                omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(order.getShippingAddress()));
                HashSet<String> addressUnique = new HashSet<>();
                addressUnique.add(shippingAddress.getAddress1());
                omsOrderSaveReqDTO.setAddress1(shippingAddress.getAddress1());
                if (!addressUnique.contains(shippingAddress.getAddress2())) {
                    omsOrderSaveReqDTO.setAddress2(shippingAddress.getAddress2());
                }
                omsOrderSaveReqDTO.setPostalCode(shippingAddress.getZip());
            }

            List<ShopifyOrderRepsVO.LineItemsDTO> lineItems = order.getLineItems();
            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            for (ShopifyOrderRepsVO.LineItemsDTO lineItem : lineItems) {
                OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                if (ObjectUtil.isNotEmpty(existShopProductMap.get(omsOrderSaveReqDTO.getShopId()))
                    && ObjectUtil.isNotEmpty(existShopProductMap.get(omsOrderSaveReqDTO.getShopId()).get(lineItem.getSku()))
                ) {
                    omsOrderItemSaveReqDTO.setShopProductId(existShopProductMap.get(omsOrderSaveReqDTO.getShopId()).get(lineItem.getSku()).getId());
                }
                omsOrderItemSaveReqDTO.setExternalId(lineItem.getId().toString());
                omsOrderItemSaveReqDTO.setQty(lineItem.getQuantity());
                omsOrderItemSaveReqDTO.setPrice(new BigDecimal(lineItem.getPrice()));
                omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
            }
            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }

    public LocalDateTime parseTime(String time) {

        // 解析字符串为 OffsetDateTime
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(time);

        // 转换为目标时区（例如：+08:00 对应东八区）
        ZonedDateTime zonedDateTime = offsetDateTime.atZoneSameInstant(ZoneId.of("Asia/Shanghai"));

        // 提取 LocalDateTime
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

        // 定义输出格式并格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = localDateTime.format(formatter);
        return LocalDateTime.parse(formattedDateTime, formatter);
    }
}
