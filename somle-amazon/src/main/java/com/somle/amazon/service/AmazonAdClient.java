package com.somle.amazon.service;

import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.model.AmazonShop;
import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.common.util.json.JSONArray;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.RequestX;
import com.somle.framework.common.util.web.WebUtils;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@AllArgsConstructor
@Setter
public class AmazonAdClient {

    public AmazonAccount account;


    public Stream<AmazonShop> getShops() {
        return account.getSellers().stream().flatMap(seller->seller.getShops().stream());
    }

    public AmazonShop getShop(String countryCode) {
        return getShops().filter(shop->shop.getCountry().getCode().equals(countryCode)).findFirst().get();
    }

    private Map<String, String> generateHeaders(AmazonShop shop) {
        String contentType = "application/vnd.createasyncreportrequest.v3+json";
        Map<String, String> headers = Map.of(
            "Amazon-Advertising-API-Scope", shop.getProfileId(),
            "Amazon-Advertising-API-ClientId", shop.getSeller().getAccount().getAdClientId(),
            "Authorization", shop.getSeller().getAdAccessToken(),
            "Content-Type", contentType,
            "Accept", contentType
        );
        return headers;
    }

    public Stream<JSONArray> getAllAdReport(LocalDate dataDate) {
        var reportIdMap = getShops().collect(Collectors.toMap(
                shop->shop,
                shop->createAdReport(shop, dataDate)
        ));
        // usually take more than 5 mins
        CoreUtils.sleep(300000);
        return reportIdMap.entrySet().stream().map(entry->getReport(entry.getKey(), entry.getValue()));
    }






    public String createAdReport(AmazonShop shop, LocalDate dataDate) {
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

        return createReport(shop, params, dataDate);
    }


    @Transactional(readOnly = true)
    public JSONArray getReport(AmazonShop shop, JSONObject payload, LocalDate dataDate) {
        var reportId = createReport(shop, payload, dataDate);
        return getReport(shop, reportId);
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public String createReport(AmazonShop shop, JSONObject payload, LocalDate dataDate) {
        JSONObject updateDict = JsonUtils.newObject();
        updateDict.put("startDate", dataDate.toString());
        updateDict.put("endDate", dataDate.toString());
        payload.putAll(updateDict);

        String partialUrl = "/reporting/reports";
        String endpoint = shop.getSeller().getRegion().getAdEndPoint();
        String fullUrl = endpoint + partialUrl;


        // Create report
        String reportId = null;
        while (reportId == null) {
            log.info("Creating ad report...");
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(fullUrl)
                .headers(generateHeaders(shop))
                .payload(payload)
                .build();
            var response = WebUtils.sendRequest(request);
            switch (response.code()) {
                case 200:
                    break;
                case 425:
                    throw new RuntimeException("The Request is a duplicate");
                case 429:
                    log.info("Received 429 Too Many Requests. Retrying...");
                    CoreUtils.sleep(3000);
                    continue;
                default:
                    throw new RuntimeException("Unknown response code in creating report: " + response.body().string());
            }
            var responseBody = WebUtils.parseResponse(response, JSONObject.class);
            reportId = responseBody.getString("reportId");
        }
        log.info("Got report ID for shop: " + shop.getCountry().getCode());
        return reportId;
    }

    @Transactional(readOnly = true)
    public JSONArray getReport(AmazonShop shop, String reportId) {

        String partialUrl = "/reporting/reports";
        String endpoint = shop.getSeller().getRegion().getAdEndPoint();
        String fullUrl = endpoint + partialUrl;


        // Check report status and get document id
        String status = null;
        String docUrl = null;
        // ResponseEntity<JSONObject> response = null;
        while (!"COMPLETED".equals(status)) {
            CoreUtils.sleep(5000);
            String reportStatusUrl = endpoint + "/reporting/reports/" + reportId;
            log.info("Checking report status...");
            var tokenExpireTime = shop.getSeller().getAdExpireTime();
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.GET)
                .url(reportStatusUrl)
                .headers(generateHeaders(shop))
                .build();
            var response = WebUtils.sendRequest(request);
            switch (response.code()) {
                case 200:
                    break;
                case 401:
                    throw new RuntimeException("Failed for shop " + shop.getCountry() + " Unauthorized error, token expired at " +  tokenExpireTime);
                case 429:
                    log.info("Received 429 Too Many Requests. Retrying...");
                    CoreUtils.sleep(10000);
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

        var contentString = WebUtils.urlToString(docUrl, "gzip");

        return JsonUtils.parseObject(contentString, JSONArray.class);
    }


    
}
