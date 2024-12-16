package com.somle.amazon.service;

import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.amazon.controller.vo.AmazonSpOrderRespVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
import com.somle.amazon.model.*;
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
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@Slf4j
@AllArgsConstructor
@Setter
public class AmazonSpClient {

    // private List<AmazonShop> shops;

    private AmazonAccount account;




    @Transactional(readOnly = true)
    public Stream<AmazonShop> getShops() {
        return account.getSellers().stream().flatMap(seller->seller.getShops().stream());
    }

    @Transactional(readOnly = true)
    public AmazonShop getShop(String countryCode) {
        return getShops().filter(shop->shop.getCountry().getCode().equals(countryCode)).findFirst().get();
    }

    public void validateResponse(AmazonSpOrderRespVO response) {
        if (!CollectionUtils.isEmpty(response.getErrors())) {
            throw new RuntimeException("Amazon sp error response: " + response);
        }
    }



    @SneakyThrows
    @Transactional(readOnly = true)
    public AmazonSpOrderRespVO getOrder(AmazonSeller seller, AmazonSpOrderReqVO vo) {
        log.info("get orders");

        String endPoint = seller.getRegion().getSpEndPoint();
        String partialUrl = "/orders/v0/orders";
        String fullUrl = endPoint + partialUrl;
        var headers = Map.of("x-amz-access-token", seller.getSpAccessToken());
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(vo)
            .headers(headers)
            .build();
        var response = WebUtils.sendRequest(request);
        var bodyString = response.body().string();
        var result = JsonUtils.parseObject(bodyString, AmazonSpOrderRespVO.class);
        validateResponse(result);
        return result;
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public Stream<AmazonSpOrderRespVO> streamOrder(AmazonSeller seller, AmazonSpOrderReqVO vo) {
        return PageUtils.getAllPages(
            getOrder(seller, vo),
            page -> !StrUtils.isEmpty(page.getPayload().getNextToken()),
            page -> {
                var nextToken = page.getPayload().getNextToken();
                var reqVO = AmazonSpOrderReqVO.builder()
                    .nextToken(nextToken)
                    .build();
                return getOrder(seller, reqVO);
            }
        );
    }


    @Transactional(readOnly = true)
    public List<AmazonSpReport> getReports(AmazonSeller seller, AmazonSpReportReqVO vo) {
        log.info("get reports");

        String endPoint = seller.getRegion().getSpEndPoint();
        String partialUrl = "/reports/2021-06-30/reports";
        String fullUrl = endPoint + partialUrl;
        var headers = Map.of("x-amz-access-token", seller.getSpAccessToken());
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .queryParams(vo)
            .headers(headers)
            .build();
        var response = WebUtils.sendRequest(request);
        var reportsString = WebUtils.parseResponse(response, JSONObject.class).get("reports");
        var reportList = JsonUtils.parseArray(reportsString, AmazonSpReport.class);
        return reportList;
    }

    @Transactional(readOnly = true)
    public Stream<String> getReportStream(AmazonSeller seller, AmazonSpReportReqVO vo, String compression) {
        return getReports(seller, vo).stream().map(report -> getReport(seller, report.getReportId(), compression));
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public String getReport(AmazonSeller seller, String reportId, String compression) {
        log.info("get report");
        int RETENTION_DAYS = 720;
        DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String endPoint = seller.getRegion().getSpEndPoint();
        var headers = Map.of("x-amz-access-token", seller.getSpAccessToken());
        String result = null;

        // Check report status and get document ID
        String status = null;
        String reportStatusUrl = endPoint + "/reports/2021-06-30/reports/" + reportId;
        String docId = null;
        while (!"DONE".equals(status)) {
            log.info("requesting for document id");
            // ResponseEntity<JSONObject> response = restTemplate.exchange(reportStatusUrl, HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class);
            // JSONObject responseBody = response.getBody();
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.GET)
                .url(reportStatusUrl)
                .headers(headers)
                .build();
            var response = WebUtils.sendRequest(request);

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
            status = responseBody.getString("processingStatus");
            log.info(status);
            switch (status) {
                case "CANCELLED":
                    result = "No data returned, get report fail.";
                    return result;
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
                .headers(headers)
                .build();
            var response = WebUtils.sendRequest(request);
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

        log.info("Document URL: {}", docUrl);

        // Use util to process the document URL
        result = WebUtils.urlToString(docUrl);


        return result;
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public String createReport(AmazonSeller seller, AmazonSpReportSaveVO vo) {
        log.info("get report");

        String endPoint = seller.getRegion().getSpEndPoint();
        String partialUrl = "/reports/2021-06-30/reports";
        String fullUrl = endPoint + partialUrl;
        var headers = Map.of("x-amz-access-token", seller.getSpAccessToken());

        // Create report
        String reportId = null;
        while (reportId == null) {
            log.info("creating report");
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(fullUrl)
                .headers(headers)
                .payload(vo)
                .build();
            var response = WebUtils.sendRequest(request);
            switch (response.code()) {
                case 202:
                    var report = WebUtils.parseResponse(response, AmazonSpReport.class);
                    reportId = report.getReportId();
                    break;
                case 403:
                    var error = WebUtils.parseResponse(response, AmazonErrorList.class);
                    switch (error.getErrors().get(0).getCode()) {
                        case "Unauthorized":
                            log.error(error.toString());
                            throw new RuntimeException("Error unauthorized");
                        default:
                            break;
                    }
                    throw new RuntimeException("Error creating report: " + error);
                case 429:
                    log.info("Received 429 Too Many Requests. Retrying...");
                    CoreUtils.sleep(3000);
                    continue;
                default:
                    throw new RuntimeException("Unknown response code: " + response.code() + "Detail: " + response.body().string());
            }
        }
        log.info("Got report ID: {}", reportId);
        return reportId;
    }

    @Transactional(readOnly = true)
    public String createAndGetReport(AmazonSeller seller, AmazonSpReportSaveVO vo, String compression) {
        String reportId = createReport(seller, vo);
        var reportString = getReport(seller, reportId, compression);
        return reportString;
    }
}

