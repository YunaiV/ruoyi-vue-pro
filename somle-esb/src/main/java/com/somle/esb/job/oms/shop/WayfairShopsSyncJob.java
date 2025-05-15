package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.WayfairToOmsConverter;
import com.somle.wayfair.service.WayfairService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WayfairShopsSyncJob extends BaseShopsSyncJob {

    @Resource
    WayfairService wayfairService;

    @Resource
    WayfairToOmsConverter wayfairToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return wayfairService.clients.stream()
            .map(client -> wayfairToOmsConverter.toShops(client.getToken()))
            .toList();
    }
}
