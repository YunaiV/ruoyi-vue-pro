package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.cdiscount.service.CdiscountService;
import com.somle.esb.converter.oms.CdiscountToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CdiscountShopProductsSyncJob extends BaseShopProductsSyncJob {
    @Resource
    private CdiscountService cdiscountService;
    @Resource
    private CdiscountToOmsConverter cdiscountToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {
        return cdiscountService.clients.stream()
            .flatMap(client -> cdiscountToOmsConverter.toProducts(client.getAllProducts(), client.getSeller()).stream())
            .toList();
    }
}
