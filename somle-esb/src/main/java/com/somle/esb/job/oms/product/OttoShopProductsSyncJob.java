package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.OttoToOmsConverter;
import com.somle.otto.service.OttoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OttoShopProductsSyncJob extends BaseShopProductsSyncJob {
    @Resource
    OttoService ottoService;
    @Resource
    OttoToOmsConverter ottoToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {
        return ottoService.ottoClients.stream()
            .flatMap(
                client -> ottoToOmsConverter.toProducts(client.getAllProducts(), client.ottoAccount).stream()
            )
            .toList();
    }
}
