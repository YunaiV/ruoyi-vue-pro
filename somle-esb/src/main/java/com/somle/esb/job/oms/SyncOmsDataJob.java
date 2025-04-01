package com.somle.esb.job.oms;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import com.somle.esb.client.oms.SyncOmsClient;
import com.somle.esb.enums.oms.SalesPlatformEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SyncOmsDataJob implements JobHandler {

    @Resource
    protected ApplicationContext applicationContext;

    @Resource
    protected OmsShopService omsShopService;

    @Resource
    protected OmsShopProductService omsShopProductService;
    protected Map<SalesPlatformEnum, SyncOmsClient> syncOmsClientMap;


    @PostConstruct
    public void init() {
        // 获得所有注册的SyncOmsClient类型的 Spring Bean
        Map<String, SyncOmsClient> stringSyncOmsClientMap = applicationContext.getBeansOfType(SyncOmsClient.class);
        this.syncOmsClientMap = stringSyncOmsClientMap.values().stream()
            .collect(Collectors.toMap(t -> t.getSalesPlatform(), t -> t));
    }

    @Override
    public String execute(String param) throws Exception {
        return "";
    }
}
