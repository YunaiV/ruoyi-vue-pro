package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.Home24ToOmsConverter;
import com.somle.home24.service.Home24Service;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Home24ShopsSyncJob extends BaseShopsSyncJob {
    @Resource
    Home24Service home24Service;
    @Resource
    Home24ToOmsConverter home24ToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return home24Service.clients.stream()
            .map(client -> home24ToOmsConverter.toShops(client.getShopInformation()))
            .toList();
    }
}
