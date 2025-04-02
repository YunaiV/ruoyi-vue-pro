package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.AbstractToOmsConverter;
import com.somle.shopify.service.ShopifyClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ShopProductsSyncJob<SHOP,PRODUCT> implements JobHandler {

    @Resource
    OmsShopProductApi omsShopProductApi;
    @Override
    public String execute(String param) throws Exception {
        return "";
    }

    /**
     * 同步店铺产品资料
     **/
    public void syncShopProducts(AbstractToOmsConverter<SHOP,PRODUCT> converter, List<PRODUCT> shopProducts, ShopifyClient shopifyClient) {
        List<OmsShopProductSaveReqDTO> omsShopProducts = converter.toProduct(shopProducts, shopifyClient);
        omsShopProductApi.createOrUpdateShopByPlatform(omsShopProducts);
    }

}
