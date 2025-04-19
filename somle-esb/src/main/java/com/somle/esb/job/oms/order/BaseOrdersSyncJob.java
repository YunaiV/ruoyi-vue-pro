package com.somle.esb.job.oms.order;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.oms.api.OmsOrderApi;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import com.somle.esb.enums.TenantId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/*
    为订单同步任务提供基础实现，并指定需要实现的抽象方法
 */
@Slf4j
@Component
public abstract class BaseOrdersSyncJob implements JobHandler {
    @Resource
    OmsOrderApi omsOrderApi;

    @Override
    public String execute(String param) throws Exception {
        // 设置租户为默认租户
        TenantContextHolder.setTenantId(TenantId.DEFAULT.getId());
        omsOrderApi.createOrUpdateOrderByPlatform(listOrders());
        return "sync orders success!";
    }

    public abstract List<OmsOrderSaveReqDTO> listOrders();
}
