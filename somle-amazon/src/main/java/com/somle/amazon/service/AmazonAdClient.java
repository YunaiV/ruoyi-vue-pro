package com.somle.amazon.service;

import cn.hutool.json.JSON;
import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.model.AmazonShop;
import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.common.util.json.JSONArray;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.WebUtils;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@Slf4j
@AllArgsConstructor
@Setter
public class AmazonAdClient {

    private AmazonAccount account;


    public Stream<AmazonShop> getShops() {
        return account.getSellers().stream().flatMap(seller->seller.getShops().stream());
    }

    public AmazonShop getShop(String countryCode) {
        return getShops().filter(shop->shop.getCountry().getCode().equals(countryCode)).findFirst().get();
    }

    public Stream<JSONArray> getAllAdReport(LocalDate dataDate) {
//        return getShops().map(shop->getAdReport(shop, dataDate));
        return WebUtils.parallelRun(12, ()->
            getShops().parallel().map(shop->getAdReport(shop, dataDate))
        );
    }

    public JSONArray getAdReport(String countryCode, LocalDate dataDate) {
        return getAdReport(getShop(countryCode), dataDate);
    }


    public JSONArray getAdReport(AmazonShop shop, LocalDate dataDate) {
        List<String> baseMetric = new ArrayList<>(Arrays.asList(
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

        JSONObject params = JsonUtils.newObject();
//        params.put("startDate", null);
//        params.put("endDate", null);

        JSONObject configuration = JsonUtils.newObject();
        configuration.put("adProduct", "SPONSORED_BRANDS");
        configuration.put("groupBy", new ArrayList<>(Arrays.asList("ads")));
        configuration.put("columns", baseMetric);
        configuration.put("reportTypeId", "sbAds");
        configuration.put("timeUnit", "DAILY");
        configuration.put("format", "GZIP_JSON");

        params.put("configuration", configuration);

        return getReport(shop, params, dataDate);
    }

    @Transactional(readOnly = true)
    public JSONArray getReport(AmazonShop shop, JSONObject payload, LocalDate dataDate) {
        shop = getShop(shop.getCountry().getCode());
        var seller = shop.getSeller();
        var region = seller.getRegion();
        // log.debug(shop.getProfileId());
        // log.debug(seller.getAdAccessToken());
        // log.debug(adClientId);
        JSONObject updateDict = JsonUtils.newObject();
        updateDict.put("startDate", dataDate.toString());
        updateDict.put("endDate", dataDate.toString());
        payload.putAll(updateDict);

        String partialUrl = "/reporting/reports";
        String endpoint = region.getAdEndPoint();
        String fullUrl = endpoint + partialUrl;

        log.debug("profileId: " + shop.getProfileId());
        log.debug("adClientId " + account.getAdClientId());
        log.debug("Auth " + seller.getAdAccessToken());

        String contentType = "application/vnd.createasyncreportrequest.v3+json";
        Map<String, String> headers = Map.of(
            "Amazon-Advertising-API-Scope", shop.getProfileId(),
            "Amazon-Advertising-API-ClientId", account.getAdClientId(),
            "Authorization", seller.getAdAccessToken(),
            "Content-Type", contentType,
            "Accept", contentType
        );





        // Create report
        String reportId = null;
        while (reportId == null) {
            log.info("Creating ad report...");
            var response = WebUtils.postRequest(fullUrl, Map.of(), headers, payload);
            switch (response.code()) {
                case 200:
                    break;
                case 425:
                    throw new RuntimeException("The Request is a duplicate");
                default:
                    throw new RuntimeException("Unknown response code in creating report: " + response.code());
            }
            var responseBody = WebUtils.parseResponse(response, JSONObject.class);
            log.debug(response.toString());
            reportId = responseBody.getString("reportId");
        }
        log.info("Got report ID");

        // Check report status and get document id
        String status = null;
        String docUrl = null;
        // ResponseEntity<JSONObject> response = null;
        while (!"COMPLETED".equals(status)) {
            String reportStatusUrl = endpoint + "/reporting/reports/" + reportId;
            log.info("Checking report status...");
            // response = restTemplate.exchange(reportStatusUrl, HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class);
            var response = WebUtils.getRequest(reportStatusUrl, Map.of(), headers);
            switch (response.code()) {
                case 200:
                    break;
                case 429:
                    log.info("Received 429 Too Many Requests. Retrying...");
                    CoreUtils.sleep(3000);
                    continue;
                default:
                    throw new RuntimeException("Http error code: " + response + response.body());
            }
            var responseBody = WebUtils.parseResponse(response, JSONObject.class);
            log.debug(responseBody.toString());
            status = responseBody.getString("status");
            log.info(status);
            switch (status) {
                case "CANCELLED":
                    log.info("No data returned.");
                    return null;
                case "PENDING":
                    break;
                case "PROCESSING":
                    break;
                case "COMPLETED":
                    docUrl = responseBody.getString("url");
                    break;
                default:
                    throw new RuntimeException("Unknown status code: " + status);
            }
        }
        log.info("Got doc url {}", docUrl);

        JSONArray contentDict = WebUtils.urlToDict(docUrl, "gzip", JSONArray.class);

        return contentDict;
    }


    
}
