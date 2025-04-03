package com.somle.esb.converter.oms;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.enums.PlatformEnum;
import com.somle.shopify.service.ShopifyClient;
import lombok.Getter;

import java.util.List;


/**
 * @Description: 公共转换器，向销售平台获取数据(如店铺信息，产品信息等)，转换为OMS模块DO对象
 */
@Getter
public abstract class AbstractToOmsConverter<SHOP, PRODUCT> {
    private PlatformEnum platform;

    /**
     * @param salesPlatform 销售平台
     * @Description: 转换器构造方法，初始化时将key和对应的类型转换器注册到map中
     * @return:
     */
    protected AbstractToOmsConverter(PlatformEnum salesPlatform) {
        this.platform = salesPlatform;
    }


    /**
     * @param shop 从销售平台获取到的商店信息DTO
     * @Description: 转换成OMS商店信息DO
     * @return: @return {@link List }<{@link OmsShopSaveReqDTO }>
     */
    public abstract List<OmsShopSaveReqDTO> toShop(List<SHOP> shop);

    public abstract List<OmsShopProductSaveReqDTO> toProduct(List<PRODUCT> product, ShopifyClient shopifyClient);

}







