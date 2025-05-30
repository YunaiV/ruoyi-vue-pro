package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.TikTokToOmsConverter;
import com.somle.tiktok.service.TikTokService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TikTokShopProductsSyncJob extends BaseShopProductsSyncJob {

    @Resource
    TikTokService tikTokService;
    @Resource
    TikTokToOmsConverter tikTokToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {
        return tikTokService.tikTokClients.stream().flatMap(client -> {
            return tikTokToOmsConverter.toProducts(client.getAllProducts(), client).stream();
        }).toList();
    }
}
