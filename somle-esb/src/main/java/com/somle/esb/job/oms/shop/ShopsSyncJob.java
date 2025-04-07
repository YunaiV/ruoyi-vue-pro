package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.AbstractToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;

@Slf4j
@Component
public class ShopsSyncJob<SHOP,PRODUCT> implements JobHandler {

    @Resource
    OmsShopApi omsShopApi;

    @Override
    public String execute(String param) throws Exception {
        return "";
    }


    /**
     * 同步店铺资料
     **/
    public void syncShops(AbstractToOmsConverter<SHOP,PRODUCT> converter, List<SHOP> shops) {
        List<OmsShopSaveReqDTO> omsShops = converter.toShop(shops);
        omsShopApi.createOrUpdateShopByPlatform(omsShops);
    }


}
