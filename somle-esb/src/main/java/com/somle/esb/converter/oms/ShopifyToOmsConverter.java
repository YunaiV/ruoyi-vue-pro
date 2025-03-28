package com.somle.esb.converter.oms;

import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import cn.iocoder.yudao.module.oms.enums.ErpShopType;
import com.somle.esb.enums.SalesPlatform;
import com.somle.esb.model.ShopProfileDTO;
import com.somle.shopify.model.reps.ShopifyShopRepsDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
public class ShopifyToOmsConverter<IN,OUT> extends SalesPlatformToOmsConverter<IN,OUT> {

    public ShopifyToOmsConverter() {
        super(SalesPlatform.SHOPIFY);
    }

    public static LocalDateTime toLocalDateTime(String dateStr) {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        // 解析字符串为 ZonedDateTime 对象
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr, formatter);
        // 将 ZonedDateTime 转换为 Date 对象
        return zonedDateTime.toLocalDateTime();

    }

    @Override
    protected List<OUT> toModelInternal(ShopProfileDTO<IN> shopInfoDTO) {
        return null;
    }

    @Override
    protected List<OmsShopDO> toOmsShopDO(ShopProfileDTO<IN> shopInfoDTO) {
        List<ShopifyShopRepsDTO> shopList = (List<ShopifyShopRepsDTO>) shopInfoDTO.getPayload();
        List<OmsShopDO> omsShopDOs = shopList.stream().map(shopifyShopRepsDTO -> {
            OmsShopDO shopDO = new OmsShopDO();
            shopDO.setName(null);
            shopDO.setPlatformShopName(shopifyShopRepsDTO.getName());
            shopDO.setCode(null);
            shopDO.setPlatformShopCode(shopifyShopRepsDTO.getId().toString());
            shopDO.setPlatformCode(shopInfoDTO.getSalesPlatform().name());
            return shopDO;
        }).toList();
        return omsShopDOs;
    }
}
