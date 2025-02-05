package com.somle.amazon.service;

import com.somle.amazon.controller.vo.AmazonAdAccountRespVO;
import com.somle.amazon.controller.vo.AmazonAdProfileRespVO;
import com.somle.amazon.model.AmazonAdAuthDO;
import com.somle.amazon.model.enums.AmazonRegion;
import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.common.util.json.JSONArray;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.RequestX;
import com.somle.framework.common.util.web.WebUtils;

import com.fasterxml.jackson.core.type.TypeReference;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@AllArgsConstructor
@Getter
@Setter
public class AmazonAdClient {

//    public AmazonAccount account;

    private AmazonAdAuthDO auth;

    private String getEndPoint() {
        return AmazonRegion.findByCode(auth.getRegionCode()).getAdUrl();
    }



    private Map<String, String> generateHeaders() {
        Map<String, String> headers = Map.of(
            "Amazon-Advertising-API-ClientId", auth.getClientId(),
            "Authorization", auth.getAccessToken()
        );
        return headers;
    }


    private Map<String, String> generateHeaders(Long profileId) {
        String contentType = "application/vnd.createasyncreportrequest.v3+json";
        Map<String, String> headers = Map.of(
            "Amazon-Advertising-API-Scope", profileId.toString(),
            "Amazon-Advertising-API-ClientId", auth.getClientId(),
            "Authorization", auth.getAccessToken(),
            "Content-Type", contentType,
            "Accept", contentType
        );
        return headers;
    }



    public AmazonAdAccountRespVO listAccounts() {
        JSONObject payload = JsonUtils.newObject();

        String partialUrl = "/adsAccounts/list";
        String endpoint = getEndPoint();
        String fullUrl = endpoint + partialUrl;

        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(fullUrl)
            .headers(generateHeaders())
            .payload(payload)
            .build();
        return WebUtils.sendRequest(request, AmazonAdAccountRespVO.class);
    }

    public List<AmazonAdProfileRespVO> listProfiles() {
        JSONObject payload = JsonUtils.newObject();

        String partialUrl = "/v2/profiles";
        String endpoint = getEndPoint();
        String fullUrl = endpoint + partialUrl;

        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .headers(generateHeaders())
            .payload(payload)
            .build();
        try(var response = WebUtils.sendRequest(request)){
            var responseBody = WebUtils.parseResponse(response, new TypeReference<List<AmazonAdProfileRespVO>>() {});
            return responseBody;
        }
    }

    public List<AmazonAdProfileRespVO> listSellerProfiles() {
        return listProfiles().stream().filter(profile -> "seller".equals(profile.getAccountInfo().getType())).collect(Collectors.toList());
    }


//    public JSONObject getProfile() {
//
//    }

    public JSONObject listPortfolios(Long profileId) {
        JSONObject payload = JsonUtils.newObject();

        String partialUrl = "/v2/profiles";
        String endpoint = getEndPoint();
        String fullUrl = endpoint + partialUrl;

        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(fullUrl)
            .headers(generateHeaders(profileId))
            .payload(payload)
            .build();
        try(var response = WebUtils.sendRequest(request)){
            var responseBody = WebUtils.parseResponse(response, JSONObject.class);
            log.info(responseBody.toString());
            return responseBody;
        }
    }

    public Stream<JSONArray> createAndGetAllAdReport(LocalDate dataDate) {
        var reportIdMap = listSellerProfiles().stream().collect(Collectors.toMap(
            profile->profile,
            profile->createAdReport(profile.getProfileId(), dataDate)
        ));
        // usually take more than 5 mins
        CoreUtils.sleep(300000);
        return reportIdMap.entrySet().stream()
            .map(entry->getReport(entry.getKey().getProfileId(), entry.getValue()));
    }

    public JSONArray createAndGetReport(Long profileId, JSONObject payload, LocalDate dataDate) {
        var reportId = createReport(profileId, payload, dataDate);
        return getReport(profileId, reportId);
    }



    // return report id
    public String createAdReport(Long profileId, LocalDate dataDate) {
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

        return createReport(profileId, params, dataDate);
    }




    // return report id
    @SneakyThrows
    public String createReport(Long profileId, JSONObject payload, LocalDate dataDate) {
        JSONObject updateDict = JsonUtils.newObject();
        updateDict.put("startDate", dataDate.toString());
        updateDict.put("endDate", dataDate.toString());
        payload.putAll(updateDict);

        String partialUrl = "/reporting/reports";
        String endpoint = getEndPoint();
        String fullUrl = endpoint + partialUrl;


        // Create report
        String reportId = null;
        while (reportId == null) {
            log.info("Creating ad report...");
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(fullUrl)
                .headers(generateHeaders(profileId))
                .payload(payload)
                .build();
            try(var response = WebUtils.sendRequest(request)){
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
        }
        log.info("Got report ID for profileId: " + profileId);
        return reportId;
    }

    public JSONArray getReport(Long profileId, String reportId) {

        String partialUrl = "/reporting/reports";
        String endpoint = getEndPoint();
        String fullUrl = endpoint + partialUrl;


        // Check report status and get document id
        String status = null;
        String docUrl = null;
        // ResponseEntity<JSONObject> response = null;
        while (!"COMPLETED".equals(status)) {
            CoreUtils.sleep(5000);
            String reportStatusUrl = endpoint + "/reporting/reports/" + reportId;
            log.info("Checking report status...");
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.GET)
                .url(reportStatusUrl)
                .headers(generateHeaders(profileId))
                .build();
            JSONObject responseBody = null;
            try(var response = WebUtils.sendRequest(request);){
                switch (response.code()) {
                    case 200:
                        break;
                    case 401:
                        throw new RuntimeException("Failed for profileId " + profileId + " Unauthorized error, token expired");
                    case 429:
                        log.info("Received 429 Too Many Requests. Retrying...");
                        CoreUtils.sleep(10000);
                        continue;
                    default:
                        throw new RuntimeException("Http error code: " + response + response.body());
                }
                 responseBody = WebUtils.parseResponse(response, JSONObject.class);
            }
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
