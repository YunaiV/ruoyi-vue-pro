package com.somle.amazon.service;

import com.somle.amazon.controller.vo.*;
import com.somle.amazon.model.*;
import com.somle.amazon.model.enums.*;

//import com.somle.amazon.repository.AmazonSellerRepository;
import com.somle.framework.common.util.collection.CollectionUtils;
import com.somle.framework.common.util.collection.PageUtils;
import com.somle.framework.common.util.general.CoreUtils;

import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.string.StrUtils;
import com.somle.framework.common.util.web.RequestX;
import com.somle.framework.common.util.web.WebUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.net.SocketTimeoutException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@Slf4j
@AllArgsConstructor
@Getter
@Setter
public class AmazonSpClient {


    private AmazonSpAuthDO auth;

    private String getEndPoint() {
        return AmazonRegion.findByCode(auth.getRegionCode()).getSpUrl();
    }

    private Map<String, String> generateHeaders(AmazonSpAuthDO auth) {
        var headers = Map.of("x-amz-access-token", auth.getAccessToken());
        return headers;
    }

//    @Transactional(readOnly = true)
//    public JSONObject getAccount(AmazonSeller seller) {
//        String endPoint = seller.getRegion().getSpEndPoint();
//        String partialUrl = "/sellers/v1/account";
//        String fullUrl = endPoint + partialUrl;
//        var request = RequestX.builder()
//            .requestMethod(RequestX.Method.GET)
//            .url(fullUrl)
//            .headers(generateHeaders(seller))
//            .build();
//        return WebUtils.sendRequest(request, JSONObject.class);
//    }

    public List<AmazonSpMarketplaceParticipationVO> getMarketplaceParticipations() {
        String endPoint = getEndPoint();
        String partialUrl = "/sellers/v1/marketplaceParticipations";
        String fullUrl = endPoint + partialUrl;



        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .headers(generateHeaders(auth))
            .build();
        var response = WebUtils.sendRequest(request, AmazonSpMarketplaceParticipationRespVO.class);
        return response.getPayload();
    }




//    @Transactional(readOnly = true)
//    public Stream<AmazonShop> getShops() {
//        return account.getSellers().stream().flatMap(seller->seller.getShops().stream());
//    }

//    @Transactional(readOnly = true)
//    public AmazonShop getShop(String countryCode) {
//        return getShops().filter(shop->shop.getCountry().getCode().equals(countryCode)).findFirst().get();
//    }
//
    @SneakyThrows
    public String searchListingsItems(AmazonSpListingReqVO reqVO) {
        String endPoint = getEndPoint();
        String partialUrl = "/listings/2021-08-01/items/" + auth.getSellerId();
        String fullUrl = endPoint + partialUrl;



        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(reqVO)
            .headers(generateHeaders(auth))
            .build();
        try(var response = WebUtils.sendRequest(request)){
            var bodyString = response.body().string();
//            var result = JsonUtils.parseObject(bodyString, AmazonSpOrderRespVO.class);
//            validateResponse(result);
            return bodyString;
        }
    }



    public void validateResponse(AmazonSpOrderRespVO response) {
        if (!CollectionUtils.isEmpty(response.getErrors())) {
            throw new RuntimeException("Amazon sp error response: " + response);
        }
    }



    @SneakyThrows
    public AmazonSpOrderRespVO getOrder(AmazonSpOrderReqVO vo) {
        log.info("get orders");

        String endPoint = getEndPoint();
        String partialUrl = "/orders/v0/orders";
        String fullUrl = endPoint + partialUrl;
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(vo)
            .headers(generateHeaders(auth))
            .build();
        try(var response = WebUtils.sendRequest(request)){
            var bodyString = response.body().string();
            var result = JsonUtils.parseObject(bodyString, AmazonSpOrderRespVO.class);
            validateResponse(result);
            return result;
        }
    }

    @SneakyThrows
    public Stream<AmazonSpOrderRespVO> streamOrder(AmazonSpOrderReqVO vo) {
        return PageUtils.getAllPages(
            getOrder(vo),
            page -> !StrUtils.isEmpty(page.getPayload().getNextToken()),
            page -> {
                var nextToken = page.getPayload().getNextToken();
                var reqVO = AmazonSpOrderReqVO.builder()
                    .nextToken(nextToken)
                    .build();
                return getOrder(reqVO);
            }
        );
    }


    public List<AmazonSpReportRespVO> getReports(AmazonSpReportReqVO vo) {
        log.info("get reports");

        String endPoint = getEndPoint();
        String partialUrl = "/reports/2021-06-30/reports";
        String fullUrl = endPoint + partialUrl;
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(vo)
            .headers(generateHeaders(auth))
            .build();
        try(var response = WebUtils.sendRequest(request)){
            var reportsString = WebUtils.parseResponse(response, JSONObject.class).get("reports");
            var reportList = JsonUtils.parseArray(reportsString, AmazonSpReportRespVO.class);
            return reportList;
        }
    }

    public Stream<String> getReportStream(AmazonSpReportReqVO vo, String compression) {
        return getReports(vo).stream().map(report -> getReport(report.getReportId(), compression));
    }

    @SneakyThrows
    public String getReport(String reportId, String compression) {
        log.info("get report");
        int RETENTION_DAYS = 720;
        DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String endPoint = getEndPoint();
        String result = null;

        // Check report status and get document ID
        String status = null;
        String reportStatusUrl = endPoint + "/reports/2021-06-30/reports/" + reportId;
        String docId = null;
        while (!"DONE".equals(status)) {
            log.info("requesting for document id");
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.GET)
                .headers(generateHeaders(auth))
                .url(reportStatusUrl)
                .build();
            JSONObject responseBody = null;
            try(var response = WebUtils.sendRequest(request)){
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
                 responseBody = WebUtils.parseResponse(response, JSONObject.class);
            }
            status = responseBody.getString("processingStatus");
            log.info(status);
            switch (status) {
                case "CANCELLED":
                    throw new RuntimeException("No data returned, get report fail.");
                case "IN_QUEUE":
                    break;
                case "IN_PROGRESS":
                    break;
                case "DONE":
                    docId = responseBody.getString("reportDocumentId");
                    break;
                default:
                    throw new RuntimeException("Unknown status code: " + status);
            }

        }

        // Use document ID to get download URL
        String docUrl = null;
        String documentUrl = endPoint + "/reports/2021-06-30/documents/" + docId;
        while (docUrl == null) {
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.GET)
                .url(documentUrl)
                .headers(generateHeaders(auth))
                .build();
            try(var response = WebUtils.sendRequest(request)){
                switch (response.code()) {
                    case 200:
                        JSONObject responseBody = JsonUtils.parseObject(response.body().string(), JSONObject.class);
                        docUrl = responseBody.getString("url");
                    case 429:
                        log.info("Received 429 Too Many Requests. Retrying...");
                        CoreUtils.sleep(3000);
                        continue;
                    default:
                        throw new RuntimeException("Unknown reponse code: " + response + response.body());
                }
            }
        }

        log.info("Document URL: {}", docUrl);

        // Use util to process the document URL
        result = WebUtils.urlToString(docUrl, compression);


        return result;
    }

    @SneakyThrows
    public String createReport(AmazonSpReportSaveVO vo) {

        String endPoint = getEndPoint();
        String partialUrl = "/reports/2021-06-30/reports";
        String fullUrl = endPoint + partialUrl;

        // Create report
        String reportId = null;
        while (reportId == null) {
            log.info("creating report");
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(fullUrl)
                .headers(generateHeaders(auth))
                .payload(vo)
                .build();
            reportId = CoreUtils.retry(ctx -> {
                try(var response = WebUtils.sendRequest(request)){
                    switch (response.code()) {
                        case 202:
                            var report = WebUtils.parseResponse(response, AmazonSpReportRespVO.class);
                            return report.getReportId();
                        case 403:
                            var error = WebUtils.parseResponse(response, AmazonSpErrorListVO.class);
                            switch (error.getErrors().get(0).getCode()) {
                                case "Unauthorized":
                                    log.error(error.toString());
                                    throw new RuntimeException("Error unauthorized");
                                default:
                                    break;
                            }
                            throw new RuntimeException("Error creating report: " + error);
                        case 429:
                            throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, response.body().string());
                        default:
                            throw new RuntimeException("Unknown response code: " + response.code() + "Detail: " + response.body().string());
                    }
                }
            });

        }
        log.info("Got report ID: {}", reportId);
        return reportId;
    }

    public String createAndGetReport(AmazonSpReportSaveVO vo, String compression) {
        String reportId = createReport(vo);
        var reportString = getReport(reportId, compression);
        return reportString;
    }
}

