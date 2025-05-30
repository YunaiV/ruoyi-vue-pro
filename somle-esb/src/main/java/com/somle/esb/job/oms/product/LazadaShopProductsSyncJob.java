package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.LazadaToOmsConverter;
import com.somle.lazada.service.LazadaService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LazadaShopProductsSyncJob extends BaseShopProductsSyncJob {
    @Resource
    private LazadaService lazadaService;
    @Resource
    private LazadaToOmsConverter lazadaToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {
        return lazadaService.clients.stream().map(
            client -> lazadaToOmsConverter.toProducts(client.getAllProducts(), client)
        ).flatMap(List::stream).toList();
    }
}
