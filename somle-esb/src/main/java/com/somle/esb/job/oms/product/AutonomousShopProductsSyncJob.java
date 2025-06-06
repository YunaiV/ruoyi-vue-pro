package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.autonomous.service.AutonomousService;
import com.somle.esb.converter.oms.AutonomousToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AutonomousShopProductsSyncJob extends BaseShopProductsSyncJob {
    @Resource
    AutonomousService autonomousService;
    @Resource
    AutonomousToOmsConverter autonomousToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {
        return autonomousService.autonomousClients.stream()
            .flatMap(client -> autonomousToOmsConverter.toProducts(client.getAllProduct(), client.autonomousAccount).stream())
            .toList();
    }
}
