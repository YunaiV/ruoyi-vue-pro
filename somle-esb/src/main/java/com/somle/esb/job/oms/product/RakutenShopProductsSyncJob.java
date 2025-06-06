package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.RakutenToOmsConverter;
import com.somle.rakuten.service.RakutenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RakutenShopProductsSyncJob extends BaseShopProductsSyncJob {

    @Resource
    RakutenService rakutenService;
    @Resource
    RakutenToOmsConverter rakutenToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {
        return rakutenService.rakutenClients.stream().flatMap(rakutenClient -> {
            return rakutenToOmsConverter.toProducts(rakutenClient.getAllProducts(), rakutenClient.entity).stream();
        }).toList();
    }
}
