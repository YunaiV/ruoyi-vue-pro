package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonAdReportReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AmazonadSbAdReportDataJob extends AmazonadDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var baseMetric = new ArrayList<>(List.of(
            "addToCart", "addToCartClicks", "addToCartRate", "adGroupId", "adGroupName", "adId",
            "brandedSearches", "brandedSearchesClicks", "campaignBudgetAmount", "campaignBudgetCurrencyCode",
            "campaignBudgetType", "campaignId", "campaignName", "campaignStatus", "clicks", "cost", "costType",
            "date", "detailPageViews", "detailPageViewsClicks", "eCPAddToCart", "impressions",
            "newToBrandDetailPageViewRate", "newToBrandDetailPageViews", "newToBrandDetailPageViewsClicks",
            "newToBrandECPDetailPageView", "newToBrandPurchases", "newToBrandPurchasesClicks",
            "newToBrandPurchasesPercentage", "newToBrandPurchasesRate", "newToBrandSales", "newToBrandSalesClicks",
            "newToBrandSalesPercentage", "newToBrandUnitsSold", "newToBrandUnitsSoldClicks",
            "newToBrandUnitsSoldPercentage", "purchases", "purchasesClicks", "purchasesPromoted", "sales",
            "salesClicks", "salesPromoted", "unitsSold", "unitsSoldClicks", "video5SecondViewRate", "video5SecondViews",
            "videoCompleteViews", "videoFirstQuartileViews", "videoMidpointViews", "videoThirdQuartileViews",
            "videoUnmutes", "viewabilityRate", "viewableImpressions"
        ));

        baseMetric.remove("startDate");
        baseMetric.remove("endDate");

        var configuration = AmazonAdReportReqVO.Configuration.builder()
            .adProduct("SPONSORED_BRANDS")
            .groupBy(List.of("ads"))
            .columns(baseMetric)
            .reportTypeId("sbAds")
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
                    .tableName("sb_ad_report")
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