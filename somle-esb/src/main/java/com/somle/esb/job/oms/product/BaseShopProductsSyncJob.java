package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public abstract class BaseShopProductsSyncJob implements JobHandler {

    @Resource
    OmsShopProductApi omsShopProductApi;
    @Override
    public String execute(String param) throws Exception {
        return "";
    }

    public abstract List<OmsShopProductSaveReqDTO> listProducts();

}
