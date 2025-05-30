package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.WalmartDSVToOmsConverter;
import com.somle.walmart.service.WalmartClient;
import com.somle.walmart.service.WalmartService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class WalmartDSVShopsSyncJob extends BaseShopsSyncJob {
    @Resource
    WalmartService walmartService;
    @Resource
    WalmartDSVToOmsConverter walmartDSVToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        Optional<WalmartClient> walmartClient = walmartService.walmartClients.stream()
            .filter(client -> client.token.getSvcName().equals("DSV"))
            .findFirst();
        return walmartClient.map(client -> {
            return Collections.singletonList(walmartDSVToOmsConverter.toShops(client.token));
        }).orElse(Collections.emptyList());
    }

}
