package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.ManomanoToOmsConverter;
import com.somle.manomano.service.ManomanoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ManomanoShopProductsSyncJob extends BaseShopProductsSyncJob {

    @Resource
    ManomanoService manomanoService;
    @Resource
    ManomanoToOmsConverter manomanoToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {
        return manomanoService.clients.stream()
            .flatMap(client -> manomanoToOmsConverter.toProducts(client.getAllProducts(), client.getShop()).stream())
            .toList();
    }
}
