package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonAdReportReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AmazonadSdAdvertisedProductReportDataJob extends AmazonadDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var baseMetric = new ArrayList<>(List.of(
            "addToCart", "addToCartRate", "addToCartViews", "addToCartClicks", "adGroupId", "adGroupName",
            "adId", "addToList", "addToListFromClicks", "qualifiedBorrows", "royaltyQualifiedBorrows",
            "addToListFromViews", "qualifiedBorrowsFromClicks", "qualifiedBorrowsFromViews",
            "royaltyQualifiedBorrowsFromClicks", "royaltyQualifiedBorrowsFromViews", "bidOptimization",
            "brandedSearches", "brandedSearchesClicks", "brandedSearchesViews", "brandedSearchRate",
            "campaignBudgetCurrencyCode", "campaignId", "campaignName", "clicks", "cost", "cumulativeReach",
            "date", "detailPageViews", "detailPageViewsClicks", "eCPAddToCart", "eCPBrandSearch", "endDate",
            "impressions", "impressionsFrequencyAverage", "impressionsViews", "leadFormOpens", "leads",
            "linkOuts", "newToBrandDetailPageViewClicks", "newToBrandDetailPageViewRate",
            "newToBrandDetailPageViews", "newToBrandDetailPageViewViews", "newToBrandECPDetailPageView",
            "newToBrandPurchases", "newToBrandPurchasesClicks", "newToBrandSales", "newToBrandSalesClicks",
            "newToBrandUnitsSold", "newToBrandUnitsSoldClicks", "promotedAsin", "promotedSku", "purchases",
            "purchasesClicks", "purchasesPromotedClicks", "sales", "salesClicks", "salesPromotedClicks",
            "startDate", "unitsSold", "unitsSoldClicks", "videoCompleteViews", "videoFirstQuartileViews",
            "videoMidpointViews", "videoThirdQuartileViews", "videoUnmutes", "viewabilityRate",
            "viewClickThroughRate"
        ));

        baseMetric.remove("startDate");
        baseMetric.remove("endDate");

        var configuration = AmazonAdReportReqVO.Configuration.builder()
            .adProduct("SPONSORED_DISPLAY")
            .groupBy(List.of("advertiser"))
            .columns(baseMetric)
            .reportTypeId("sdAdvertisedProduct")
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
                    .tableName("sd_advertised_product_report")
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