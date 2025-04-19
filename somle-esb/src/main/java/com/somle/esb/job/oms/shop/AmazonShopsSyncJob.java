package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.amazon.service.AmazonSpService;
import com.somle.esb.converter.oms.AmazonToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AmazonShopsSyncJob extends BaseShopsSyncJob {

    @Resource
    AmazonSpService amazonSpService;
    @Resource
    AmazonToOmsConverter amazonToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return amazonSpService.clients.stream()
            .flatMap(
                client -> amazonToOmsConverter.toShops(client.getMarketplaceParticipations()).stream()
            )
            .toList();

    }
}
