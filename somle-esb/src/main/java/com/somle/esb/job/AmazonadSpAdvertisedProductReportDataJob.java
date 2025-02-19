package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonAdReportReqVO;
import com.somle.amazon.service.AmazonAdService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AmazonadSpAdvertisedProductReportDataJob extends AmazonadDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var baseMetric = new ArrayList<>(List.of(
            "date", "startDate", "endDate", "campaignName", "campaignId", "adGroupName", "adGroupId", "adId",
            "addToList", "qualifiedBorrows", "royaltyQualifiedBorrows", "portfolioId", "impressions", "clicks",
            "costPerClick", "clickThroughRate", "cost", "spend", "campaignBudgetCurrencyCode",
            "campaignBudgetAmount", "campaignBudgetType", "campaignStatus", "advertisedAsin", "advertisedSku",
            "purchases1d", "purchases7d", "purchases14d", "purchases30d", "purchasesSameSku1d",
            "purchasesSameSku7d", "purchasesSameSku14d", "purchasesSameSku30d", "unitsSoldClicks1d",
            "unitsSoldClicks7d", "unitsSoldClicks14d", "unitsSoldClicks30d", "sales1d", "sales7d", "sales14d",
            "sales30d", "attributedSalesSameSku1d", "attributedSalesSameSku7d", "attributedSalesSameSku14d",
            "attributedSalesSameSku30d", "salesOtherSku7d", "unitsSoldSameSku1d", "unitsSoldSameSku7d",
            "unitsSoldSameSku14d", "unitsSoldSameSku30d", "unitsSoldOtherSku7d",
            "kindleEditionNormalizedPagesRead14d", "kindleEditionNormalizedPagesRoyalties14d",
            "acosClicks7d", "acosClicks14d", "roasClicks7d", "roasClicks14d"
        ));

        baseMetric.remove("startDate");
        baseMetric.remove("endDate");

        var configuration = AmazonAdReportReqVO.Configuration.builder()
            .adProduct("SPONSORED_PRODUCTS")
            .groupBy(List.of("advertiser"))
            .columns(baseMetric)
            .reportTypeId("spAdvertisedProduct")
            .timeUnit("DAILY")
            .format("GZIP_JSON")
            .build();

        var payload = AmazonAdReportReqVO.builder()
            .startDate(beforeYesterday.toString())
            .endDate(beforeYesterday.toString())
            .configuration(configuration)
            .build();

        amazonAdService.createAllClients().stream()
            .flatMap(client -> client.batchCreateAndGetReport(payload))
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("sp_advertised_product_report")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });
        return "data upload success";
    }
}