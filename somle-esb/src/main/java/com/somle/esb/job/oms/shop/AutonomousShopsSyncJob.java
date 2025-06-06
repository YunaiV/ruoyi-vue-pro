package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.autonomous.service.AutonomousService;
import com.somle.esb.converter.oms.AutonomousToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AutonomousShopsSyncJob extends BaseShopsSyncJob {

    @Resource
    AutonomousService autonomousService;
    @Resource
    AutonomousToOmsConverter autonomousToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return autonomousService.autonomousClients.stream()
            .map(client -> autonomousToOmsConverter.toShops(client.autonomousAccount))
            .toList();
    }
}
