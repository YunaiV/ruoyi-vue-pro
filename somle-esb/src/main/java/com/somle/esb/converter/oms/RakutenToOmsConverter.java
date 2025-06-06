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
import com.somle.rakuten.model.pojo.RakutenTokenEntityDO;
import com.somle.rakuten.model.reps.RakutenOrderRepsVO;
import com.somle.rakuten.model.reps.RakutenProductsRepsVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_INFO_FIRST;

@Component
@Slf4j
public class RakutenToOmsConverter {

    @Resource
    OmsShopApi omsShopApi;
    @Resource
    OmsShopProductApi omsShopProductApi;
    @Resource
    OmsOrderApi omsOrderApi;

    private PlatformEnum platform = PlatformEnum.RAKUTEN;

    public OmsShopSaveReqDTO toShops(RakutenTokenEntityDO rakutenTokenEntityDO) {

        List<OmsShopDTO> existShops = omsShopApi.getByPlatformCode(this.platform.toString());
        // 使用Map存储已存在的店铺，key = platformShopCode, value = OmsShopDO
        Map<String, OmsShopDTO> existShopMap = Optional.ofNullable(existShops)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopDO -> omsShopDO.getExternalId(), omsShopDO -> omsShopDO));

        OmsShopSaveReqDTO shopDTO = new OmsShopSaveReqDTO();
        MapUtils.findAndThen(existShopMap, rakutenTokenEntityDO.getServiceSecret(), omsShopDTO -> shopDTO.setId(omsShopDTO.getId()));
        shopDTO.setName(null);
        shopDTO.setExternalName(rakutenTokenEntityDO.getServiceSecret());
        shopDTO.setCode(null);
        shopDTO.setExternalId(rakutenTokenEntityDO.getServiceSecret());
        shopDTO.setPlatformCode(this.platform.toString());
        shopDTO.setType(ShopTypeEnum.ONLINE.getType());
        return shopDTO;
    }


    public List<OmsShopProductSaveReqDTO> toProducts(List<RakutenProductsRepsVO.Item> products, RakutenTokenEntityDO rakutenTokenEntityDO) {

        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(rakutenTokenEntityDO.getServiceSecret());
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
        for (RakutenProductsRepsVO.Item product : products) {
            List<String> skus = product.getVariants().keySet().stream().toList();
            for (int i = 0; i < skus.size(); i++) {
                OmsShopProductSaveReqDTO shopProductDTO = new OmsShopProductSaveReqDTO();
                String sku = skus.get(i);
                RakutenProductsRepsVO.Variant variant = product.getVariants().get(sku);
                if ("normal-inventory".equals(sku) || variant.getMerchantDefinedSkuId() == null) {
                    continue;
                }

                MapUtils.findAndThen(existShopProductMap, variant.getMerchantDefinedSkuId(), omsShopProductDO -> shopProductDTO.setId(omsShopProductDO.getId()));
                shopProductDTO.setShopId(omsShopDTO.getId());
                shopProductDTO.setCode(variant.getMerchantDefinedSkuId());
                shopProductDTO.setExternalId(variant.getMerchantDefinedSkuId());
                shopProductDTO.setSellableQty(variant.getSellableQuantity());
                shopProductDTO.setName(product.getTitle());
                if (ObjectUtil.isNotEmpty(variant.getStandardPrice())) {
                    shopProductDTO.setPrice(new BigDecimal(variant.getStandardPrice()));
                }
                omsShopProductDOs.add(shopProductDTO);
            }
        }
        return omsShopProductDOs;
    }


    public List<OmsOrderSaveReqDTO> toOrders(List<RakutenOrderRepsVO.OrderModel> orders, RakutenTokenEntityDO rakutenTokenEntityDO) {

        if (CollectionUtil.isEmpty(orders)) {
            return CollectionUtil.empty(List.class);
        }

        OmsShopDTO omsShopDTO = omsShopApi.getShopByPlatformShopCode(rakutenTokenEntityDO.getServiceSecret());
        if (omsShopDTO == null) {
            throw exception(OMS_SYNC_SHOP_INFO_FIRST, this.platform.toString());
        }

        List<OmsOrderDTO> existOrders = omsOrderApi.getByPlatformCode(this.platform.toString());

        // 使用Map存储已存在的订单，key = sourceNo, value = OmsOrderDO
        Map<String, OmsOrderDTO> existOrderMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsOrderDTO -> omsOrderDTO.getExternalId(), omsOrderDTO -> omsOrderDTO));

        List<OmsOrderSaveReqDTO> omsOrderSaveReqDTOs = new ArrayList<>();

        for (RakutenOrderRepsVO.OrderModel order : orders) {
            OmsOrderSaveReqDTO omsOrderSaveReqDTO = new OmsOrderSaveReqDTO();
            MapUtils.findAndThen(existOrderMap, order.getOrderNumber(), omsOrderDTO -> omsOrderSaveReqDTO.setId(omsOrderDTO.getId()));
            omsOrderSaveReqDTO.setPlatformCode(this.platform.toString());
            omsOrderSaveReqDTO.setExternalId(order.getOrderNumber());
            omsOrderSaveReqDTO.setShopId(omsShopDTO.getId());
            omsOrderSaveReqDTO.setOrderCreateTime(order.getOrderDatetime());
            omsOrderSaveReqDTO.setPayTime(order.getOrderDatetime());
            if (order.getTotalPrice() != null) {
                omsOrderSaveReqDTO.setTotalPrice(new BigDecimal(order.getTotalPrice()));
            }

            RakutenOrderRepsVO.OrdererModel ordererModel = order.getOrdererModel();
            if (ordererModel != null) {
                omsOrderSaveReqDTO.setBuyerName(ordererModel.getFamilyName() + " " + ordererModel.getFirstName());
                omsOrderSaveReqDTO.setEmail(ordererModel.getEmailAddress());
                omsOrderSaveReqDTO.setState(ordererModel.getPrefecture());
                omsOrderSaveReqDTO.setCity(ordererModel.getCity());
            }


            RakutenOrderRepsVO.SenderModel senderModel = order.getPackageModelList().get(0).getSenderModel();
            if (senderModel != null) {
                omsOrderSaveReqDTO.setExternalAddress(JsonUtilsX.toJsonString(senderModel));
                omsOrderSaveReqDTO.setRecipientName(senderModel.getFamilyName() + " " + senderModel.getFirstName());
                omsOrderSaveReqDTO.setPostalCode(senderModel.getZipCode1() + "-" + senderModel.getZipCode2());
                omsOrderSaveReqDTO.setAddress1(senderModel.getSubAddress());
                omsOrderSaveReqDTO.setRecipientCountryCode("JP");
                omsOrderSaveReqDTO.setPhone(senderModel.getPhoneNumber1() + senderModel.getPhoneNumber2() + senderModel.getPhoneNumber3());
            }


            if (order.getDeliveryPrice() != null) {
                omsOrderSaveReqDTO.setShippingFee(new BigDecimal(order.getDeliveryPrice()));
            }


            List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOs = new ArrayList<>();
            order.getPackageModelList().forEach(packageModel -> {
                packageModel.getItemModelList().forEach(itemModel -> {
                    OmsOrderItemSaveReqDTO omsOrderItemSaveReqDTO = new OmsOrderItemSaveReqDTO();
                    String merchantDefinedSkuId = itemModel.getSkuModelList().get(0).getMerchantDefinedSkuId();
                    omsOrderItemSaveReqDTO.setShopProductExternalCode(merchantDefinedSkuId);
                    omsOrderItemSaveReqDTO.setQty(itemModel.getUnits());
                    omsOrderItemSaveReqDTO.setPrice(new BigDecimal(itemModel.getPrice()));
                    omsOrderItemSaveReqDTOs.add(omsOrderItemSaveReqDTO);
                });
            });

            omsOrderSaveReqDTO.setOmsOrderItemSaveReqDTOList(omsOrderItemSaveReqDTOs);
            omsOrderSaveReqDTOs.add(omsOrderSaveReqDTO);
        }
        return omsOrderSaveReqDTOs;
    }
}
