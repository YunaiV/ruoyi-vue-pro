package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.esb.converter.oms.WalmartToOmsConverter;
import com.somle.walmart.model.reps.WalmartAllProductsRepsVO;
import com.somle.walmart.model.req.WalmartAllProductsReqVO;
import com.somle.walmart.service.WalmartService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class WalmartShopProductsSyncJob extends BaseShopProductsSyncJob {

    @Resource
    private WalmartService walmartService;

    @Resource
    private WalmartToOmsConverter walmartToOmsConverter;

    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {

        List<OmsShopProductSaveReqDTO> allProducts = walmartService.walmartClients.stream()
            .filter(client -> client.token.getSvcName().equals("Walmart Marketplace"))
            .map(client -> {
                var vo = WalmartAllProductsReqVO.builder()
                    .offset(0)
                    .limit(200)
                    .build();
                List<WalmartAllProductsRepsVO.ItemResponseDTO> products = client.getAllProducts(vo);
                return walmartToOmsConverter.toProducts(products, client.token);
            }).flatMap(List::stream).toList();
        return allProducts;
    }
}
