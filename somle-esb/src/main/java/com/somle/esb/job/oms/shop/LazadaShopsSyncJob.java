package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.LazadaToOmsConverter;
import com.somle.staples.service.LazadaService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class LazadaShopsSyncJob extends BaseShopsSyncJob {
    @Resource
    private LazadaService lazadaService;
    @Resource
    private LazadaToOmsConverter lazadaToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return lazadaService.clients.stream().map(
            client -> lazadaToOmsConverter.toShops(client.getSeller())
        ).toList();
    }
}
