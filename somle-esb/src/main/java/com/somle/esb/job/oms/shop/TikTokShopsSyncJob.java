package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.TikTokToOmsConverter;
import com.somle.tiktok.service.TikTokService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TikTokShopsSyncJob extends BaseShopsSyncJob {
    @Resource
    TikTokService tikTokService;
    @Resource
    TikTokToOmsConverter tikTokToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return tikTokService.tikTokClients.stream().flatMap(client -> {
            return tikTokToOmsConverter.toShops(client.getShop()).stream();
        }).toList();
    }
}
