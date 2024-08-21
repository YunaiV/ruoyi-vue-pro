package com.somle.amazon.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.model.AmazonShop;
import com.somle.util.Util;

import lombok.AllArgsConstructor;
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
public class AmazonAdClient {

    private AmazonAccount account;

    // @Value("${amazon.client.ad.id}")
//    public String adClientId;

    // private List<AmazonShop> shops;


    // @Autowired
//    AmazonSellerRepository sellerRepository;

    // @EventListener(ApplicationReadyEvent.class)
    // @Transactional(readOnly = true)
    // public void initialize() {
    // }

    public Stream<AmazonShop> getShops() {
        return account.getSellers().stream().flatMap(seller->seller.getShops().stream());
    }

    public AmazonShop getShop(String countryCode) {
        return getShops().filter(shop->shop.getCountry().getCode().equals(countryCode)).findFirst().get();
    }

    public Stream<JSONArray> getAllAdReport(LocalDate dataDate) {
//        return getShops().map(shop->getAdReport(shop, dataDate));
        return Util.parallelRun(12, ()->
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

        JSONObject params = new JSONObject();
        params.put("startDate", null);
        params.put("endDate", null);

        JSONObject configuration = new JSONObject();
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
        JSONObject updateDict = new JSONObject();
        updateDict.put("startDate", dataDate.toString());
        updateDict.put("endDate", dataDate.toString());
        payload.putAll(updateDict);

        String partialUrl = "/reporting/reports";
        String endpoint = region.getAdEndPoint();
        String fullUrl = endpoint + partialUrl;

        

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
            // ResponseEntity<JSONObject> response = restTemplate.exchange(fullUrl, HttpMethod.POST, new HttpEntity<JSONObject>(payload, headers), JSONObject.class);
            JSONObject response = Util.postRequest(fullUrl, Map.of(), headers, payload, JSONObject.class);
            log.debug(response.toString());
            reportId = response.getString("reportId");
        }
        log.info("Got report ID");

        // Check report status and get document id
        String status = null;
        // ResponseEntity<JSONObject> response = null;
        JSONObject response = null;
        while (!"COMPLETED".equals(status)) {
            String reportStatusUrl = endpoint + "/reporting/reports/" + reportId;
            log.info("Checking report status...");
            // response = restTemplate.exchange(reportStatusUrl, HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class);
            response = Util.getRequest(reportStatusUrl, Map.of(), headers, JSONObject.class);
            log.debug(response.toString());
            status = response.getString("status");
            log.info(status);
            if ("CANCELLED".equals(status)) {
                log.info("No data returned, skip.");
                return null;
            }
        }
        String docUrl = response.getString("url");
        log.info("Got doc url {}", docUrl);

        JSONArray contentDict = Util.urlToDict(docUrl, "gzip", JSONArray.class);

        return contentDict;
    }


    
}
