package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.AbstractToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.somle.esb.enums.ErrorCodeConstants.OMS_SYNC_SHOP_INFO_LACK;

@Slf4j
@Component
public class ShopsSyncJob<SHOP> implements JobHandler {

    @Resource
    OmsShopApi omsShopApi;

    @Override
    public String execute(String param) throws Exception {
        return "";
    }


    /**
     * 同步店铺资料
     **/
    public void syncShops(AbstractToOmsConverter<SHOP> converter, List<SHOP> shops) {

        var omsShops = converter.toShop(shops);

        for (OmsShopSaveReqDTO omsShop : omsShops) {
            omsShopApi.createShop(omsShop);
        }
    }


}
