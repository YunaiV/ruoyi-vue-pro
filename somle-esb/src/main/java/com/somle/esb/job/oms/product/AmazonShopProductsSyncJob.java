package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceParticipationVO;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceVO;
import com.somle.amazon.service.AmazonSpClient;
import com.somle.amazon.service.AmazonSpService;
import com.somle.esb.converter.oms.AmazonToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AmazonShopProductsSyncJob extends BaseShopProductsSyncJob {

    @Resource
    AmazonSpService amazonSpService;

    @Resource
    AmazonToOmsConverter amazonToOmsConverter;


    @Override
    public List<OmsShopProductSaveReqDTO> listProducts() {

        List<AmazonSpClient> clients = amazonSpService.clients;
        List<OmsShopProductSaveReqDTO> allProducts = new ArrayList<>();
        for (AmazonSpClient client : clients) {
            List<String> marketplaceIds = client.getMarketplaceParticipations().stream()
                .map(AmazonSpMarketplaceParticipationVO::getMarketplace)
                .map(AmazonSpMarketplaceVO::getId)
                .toList();
            allProducts.addAll(amazonToOmsConverter.toProducts(client.getProducts(marketplaceIds), client.getAuth()));
        }
        return allProducts;
    }
}
