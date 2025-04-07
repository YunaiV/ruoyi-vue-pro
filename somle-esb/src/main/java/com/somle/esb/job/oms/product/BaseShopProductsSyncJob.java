package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.oms.api.OmsShopProductApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.enums.TenantId;
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
        // 设置租户为默认租户
        TenantContextHolder.setTenantId(TenantId.DEFAULT.getId());
        omsShopProductApi.createOrUpdateShopByPlatform(listProducts());
        return "sync shop products success!";
    }

    public abstract List<OmsShopProductSaveReqDTO> listProducts();

}
