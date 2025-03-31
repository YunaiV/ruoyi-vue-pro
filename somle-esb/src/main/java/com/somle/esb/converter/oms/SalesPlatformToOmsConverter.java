package com.somle.esb.converter.oms;

import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import com.somle.esb.client.oms.SyncOmsClient;
import com.somle.esb.enums.SalesPlatform;
import com.somle.esb.model.OmsProfileDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Description: 公共转换器，向销售平台获取数据(如店铺信息，产品信息等)，转换为OMS模块DO对象
 */

public abstract class SalesPlatformToOmsConverter<IN, OUT> {

    private static final Map<String, SalesPlatformToOmsConverter<?, ?>> CONVERTERS = new HashMap<>();


    /**
     * @param omsProfileDTO OMS配置DTO
     * @Author: gumaomao
     * @Date: 2025/03/31
     * @Description: 公共转换方法，向销售平台获取数据(如店铺信息，产品信息等)，转换为OMS模块DO对象
     * @return: @return {@link List }<{@link VO }>
     */
    public static <VO> List<VO> convert(OmsProfileDTO omsProfileDTO, SyncOmsClient<?, ?> shopProductProfileClient) {
        String key = makeKey(omsProfileDTO.getSalesPlatform());
        SalesPlatformToOmsConverter<?, ?> converter = CONVERTERS.get(key);
        if (converter == null) {
            throw new IllegalArgumentException("未找到对应的转换器:" + omsProfileDTO.getSalesPlatform().name());
        }

        switch (omsProfileDTO.getSyncOmsType()) {
            case SHOP:
                return converter.toOmsShopDO(omsProfileDTO);
            case PRODUCT:
                return converter.toOmsShopProductDO(omsProfileDTO, shopProductProfileClient);
            default:
                throw new IllegalArgumentException("未找到对应的转换方法:" + omsProfileDTO.getSalesPlatform().name());
        }
    }


    /**
     * @param salesPlatform 销售平台
     * @Description: 转换器构造方法，初始化时将key和对应的类型转换器注册到map中
     * @return:
     */
    protected SalesPlatformToOmsConverter(SalesPlatform salesPlatform) {
        CONVERTERS.put(makeKey(salesPlatform), this);
    }

    /**
     * @param salesPlatform 销售平台
     * @Description: 跟进销售平台生成key
     * @return: @return {@link String }
     */
    private static String makeKey(SalesPlatform salesPlatform) {
        return salesPlatform.name();
    }


    /**
     * @param shopInfoDTO 从销售平台获取到的商店信息DTO
     * @Description: 转换成OMS商店信息DO
     * @return: @return {@link List }<{@link OmsShopDO }>
     */
    protected abstract List<OmsShopDO> toOmsShopDO(OmsProfileDTO<IN> shopInfoDTO);

    protected abstract List<OmsShopProductDO> toOmsShopProductDO(OmsProfileDTO<IN> shopInfoDTO, SyncOmsClient<?, ?> shopProductProfileClient);

}







