package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.OttoToOmsConverter;
import com.somle.otto.service.OttoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OttoShopsSyncJob extends BaseShopsSyncJob {

    @Resource
    OttoService ottoService;

    @Resource
    OttoToOmsConverter ottoToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return ottoService.ottoClients.stream().map(client -> {
            OmsShopSaveReqDTO omsShopSaveReqDTO = ottoToOmsConverter.toShops(client.ottoAccount);
            return omsShopSaveReqDTO;
        }).toList();
    }
}
