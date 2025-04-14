package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceParticipationVO;
import com.somle.amazon.controller.vo.AmazonSpMarketplaceVO;
import com.somle.amazon.service.AmazonSpService;
import com.somle.esb.converter.oms.AmazonToOmsConverter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        return amazonSpService.clients.stream()
            .flatMap(
                client -> {
                    List<String> marketplaceIds = client.getMarketplaceParticipations().stream()
                        .map(AmazonSpMarketplaceParticipationVO::getMarketplace)
                        .map(AmazonSpMarketplaceVO::getId)
                        .toList();

                    // 对每个marketplaceId获取产品并转换为DTO
                    return marketplaceIds.stream().flatMap(marketplaceId ->
                        amazonToOmsConverter.toProducts(
                            client.getProducts(List.of(marketplaceId)), marketplaceId).stream()
                    );
                }
            )
            .toList();
    }
}
