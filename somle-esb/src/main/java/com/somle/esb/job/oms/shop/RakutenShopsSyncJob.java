package com.somle.esb.job.oms.shop;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import com.somle.esb.converter.oms.RakutenToOmsConverter;
import com.somle.rakuten.service.RakutenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RakutenShopsSyncJob extends BaseShopsSyncJob {

    @Resource
    RakutenService rakutenService;
    @Resource
    RakutenToOmsConverter rakutenToOmsConverter;

    @Override
    public List<OmsShopSaveReqDTO> listShops() {
        return rakutenService.rakutenClients.stream().map(rakutenClient -> {
            OmsShopSaveReqDTO omsShopSaveReqDTO = rakutenToOmsConverter.toShops(rakutenClient.entity);
            return omsShopSaveReqDTO;
        }).toList();
    }
}
