package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.WalmartDSVToOmsConverter;
import com.somle.walmart.model.req.WalmartAllProductsReqVO;
import com.somle.walmart.service.WalmartClient;
import com.somle.walmart.service.WalmartService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class WalmartDSVShopProductsSyncJob extends BaseShopProductsSyncJob {

    @Resource
    private WalmartService walmartService;

    @Resource
    private WalmartDSVToOmsConverter walmartDSVToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {

        Optional<WalmartClient> walmartClient = walmartService.walmartClients.stream()
            .filter(client -> client.token.getSvcName()
                .equals("DSV"))
            .findFirst();

        List<OmsShopProductSaveReqDTO> allProducts = walmartClient.map(client -> {
            var vo = WalmartAllProductsReqVO.builder()
                .offset(0)
                .limit(200)
                .build();
            return walmartDSVToOmsConverter.toProducts(client.getAllProductDetails(vo), client.token);
        }).orElse(List.of());
        return allProducts;
    }
}
