package com.somle.esb.converter.oms;

import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import com.somle.esb.enums.SalesPlatform;
import com.somle.esb.model.ShopProfileDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public abstract class SalesPlatformToOmsConverter<IN,OUT> {

    private static final Map<String, SalesPlatformToOmsConverter<?,?>> CONVERTERS = new HashMap<>();

    public static <VO> List<VO> convert(ShopProfileDTO shopInfoDTO) {
        String key = makeKey(shopInfoDTO.getSalesPlatform());
        SalesPlatformToOmsConverter<?,?> converter= CONVERTERS.get(key);
        if (converter==null) {
            throw new IllegalArgumentException("未找到对应的转换器:"+shopInfoDTO.getSalesPlatform().name());
        }

        switch (shopInfoDTO.getSyncOmsType().name()) {
            case "SHOP":
                return converter.toOmsShopDO(shopInfoDTO);
            default:
                return converter.toModel(shopInfoDTO);
        }
    }


    protected SalesPlatformToOmsConverter(SalesPlatform salesPlatform) {
        CONVERTERS.put(makeKey(salesPlatform),this);
    }

    private static String makeKey(SalesPlatform salesPlatform) {
        return salesPlatform.name();
    }

    protected List<OUT> toModel(ShopProfileDTO<IN> shopInfoDTO) {
        if (shopInfoDTO == null) {
            return null;
        }
        return toModelInternal(shopInfoDTO);
    }

    protected abstract List<OUT> toModelInternal(ShopProfileDTO<IN> shopInfoDTO);


    protected abstract List<OmsShopDO> toOmsShopDO(ShopProfileDTO<IN> shopInfoDTO);

}







