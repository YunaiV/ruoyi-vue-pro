package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.oms.api.OmsShopApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.enums.TenantId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/*
    为店铺同步任务提供基础实现，并指定需要实现的抽象方法
 */
@Slf4j
@Component
public abstract class BaseShopsSyncJob implements JobHandler {
    @Resource
    OmsShopApi omsShopApi;

    @Override
    public String execute(String param) throws Exception {
        // 设置租户为默认租户
        TenantContextHolder.setTenantId(TenantId.DEFAULT.getId());
        omsShopApi.createOrUpdateShopByPlatform(listShops());
        return "sync shops success!";
    }

    public abstract List<OmsShopSaveReqDTO> listShops();
}
