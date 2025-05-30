package com.somle.esb.job.oms.shop;


import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.ManomanoToOmsConverter;
import com.somle.manomano.service.ManomanoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ManomanoShopsSyncJob extends BaseShopsSyncJob {

    @Resource
    ManomanoService manomanoService;
    @Resource
    ManomanoToOmsConverter manomanoToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return manomanoService.clients.stream()
            .map(client -> manomanoToOmsConverter.toShops(client.getShop()))
            .toList();
    }
}
