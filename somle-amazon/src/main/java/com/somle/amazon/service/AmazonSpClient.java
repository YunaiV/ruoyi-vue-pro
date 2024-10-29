package com.somle.amazon.service;

import com.somle.amazon.controller.vo.AmazonSpReportVO;
import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.model.AmazonErrorList;
import com.somle.amazon.model.AmazonShop;
//import com.somle.amazon.repository.AmazonSellerRepository;
import com.somle.amazon.model.AmazonSpReport;
import com.somle.amazon.model.enums.ProcessingStatuses;
import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.object.BeanUtils;
import com.somle.framework.common.util.web.WebUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.persistence.*;

import java.time.LocalDate;
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

    @Transactional(readOnly = true)
    public Stream<String> getAllSettlementReport(LocalDate dataDate) {
        return getShops().map(shop->getSettlementReport(shop, dataDate));
    }

    @Transactional(readOnly = true)
    public String getSettlementReport(AmazonShop shop, LocalDate dataDate) {
        var vo = AmazonSpReportVO.builder()
                .reportTypes(List.of("GET_V2_SETTLEMENT_REPORT_DATA_FLAT_FILE"))
                .processingStatuses(List.of(ProcessingStatuses.DONE))
                .pageSize(1)
                .build();
        var report = getReports(shop, vo).get(0);
        return getReport(shop, report.getReportId(), null);
    }

    @Transactional(readOnly = true)
    public Stream<JSONObject> getAllAsinReport(LocalDate dataDate) {
        return getShops().map(shop->getAsinReport(shop, dataDate));
    }

    @Transactional(readOnly = true)
    public JSONObject getAsinReport(AmazonShop shop, LocalDate dataDate) {
        JSONObject options = JsonUtils.newObject();
        
        // Add reportType
        options.put("reportType", "GET_SALES_AND_TRAFFIC_REPORT");

        // Add marketplaceIds
        options.put("marketplaceIds", new String[] {shop.getCountry().getMarketplaceId()});

        // Create and add reportOptions
        // Create and add reportOptions
        JSONObject reportOptions = JsonUtils.newObject();
        reportOptions.put("asinGranularity", "CHILD");
        reportOptions.put("dateGranularity", "DAY");
        options.put("reportOptions", reportOptions);
        return createAndGetReport(shop, options, dataDate);
    }

    @Transactional(readOnly = true)
    public List<AmazonSpReport> getReports(AmazonShop shop, AmazonSpReportVO vo) {
        vo.setMarketplaceIds(List.of(shop.getCountry().getMarketplaceId()));
        var countryCode = shop.getCountry().getCode();
        log.info("get reports");

        String endPoint = getShop(countryCode).getCountry().getRegion().getSpEndPoint();
        String partialUrl = "/reports/2021-06-30/reports";
        String fullUrl = endPoint + partialUrl;
        var headers = Map.of("x-amz-access-token", getShop(countryCode).getSeller().getSpAccessToken());
        var response = WebUtils.getRequest(fullUrl, vo, headers);
        var reportsString = WebUtils.parseResponse(response, JSONObject.class).get("reports");
        var reportList = JsonUtils.parseArray(reportsString, AmazonSpReport.class);
        return reportList;
    }

    @Transactional(readOnly = true)
    public String getReport(AmazonShop shop, String reportId, String compression) {
        var countryCode = shop.getCountry().getCode();
        log.info("get report");
        int RETENTION_DAYS = 720;
        DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String endPoint = getShop(countryCode).getCountry().getRegion().getSpEndPoint();
        var headers = Map.of("x-amz-access-token", getShop(countryCode).getSeller().getSpAccessToken());
        String result = null;

        // Check report status and get document ID
        String status = null;
        String reportStatusUrl = endPoint + "/reports/2021-06-30/reports/" + reportId;
        String docId = null;
        while (!"DONE".equals(status)) {
            log.info("requesting for document id");
            // ResponseEntity<JSONObject> response = restTemplate.exchange(reportStatusUrl, HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class);
            // JSONObject responseBody = response.getBody();
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
            try {
                // ResponseEntity<JSONObject> response = restTemplate.exchange(documentUrl, HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class);
                // JSONObject responseBody = response.getBody();
                JSONObject response = WebUtils.getRequest(documentUrl, Map.of(), headers, JSONObject.class);
                JSONObject responseBody = response;
                docUrl = responseBody.getString("url");
            } catch (HttpClientErrorException.TooManyRequests e) {
                log.info("Received 429 Too Many Requests. Retrying...");
                try {
                    Thread.sleep(3000); // Sleep for 3000 miliseconds before retrying
                } catch (InterruptedException ie) {
                    log.info("Thread interrupted, restoring");
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
            }
        }

        log.info("Document URL: {}", docUrl);

        // Use util to process the document URL
        result = WebUtils.urlToString(docUrl, compression);


        return result;
    }

    @Transactional(readOnly = true)
    public String createReport(AmazonShop shop, JSONObject payload, LocalDate dataDate) {
        var countryCode = shop.getCountry().getCode();
        log.info("get report");
        DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String endPoint = getShop(countryCode).getCountry().getRegion().getSpEndPoint();
        String partialUrl = "/reports/2021-06-30/reports";
        String fullUrl = endPoint + partialUrl;
        var headers = Map.of("x-amz-access-token", getShop(countryCode).getSeller().getSpAccessToken());
        JSONObject result = JsonUtils.newObject();
        payload.put("dataStartTime", dataDate.format(DATE_FORMAT));
        payload.put("dataEndTime", dataDate.format(DATE_FORMAT));

        // Create report
        String reportId = null;
        while (reportId == null) {
            log.info("creating report");
            var response = WebUtils.postRequest(fullUrl, Map.of(), headers, payload);
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
                    log.error(error.toString());
                    throw new RuntimeException("Error creating report");
                default:
                    throw new RuntimeException("Unknown response code: " + response.code());
            }
        }
        log.info("Got report ID: {}", reportId);
        return reportId;
    }

    @Transactional(readOnly = true)
    public JSONObject createAndGetReport(AmazonShop shop, JSONObject payload, LocalDate dataDate) {
        String reportId = createReport(shop, payload, dataDate);
        var reportString = getReport(shop, reportId, "gzip");
        return JsonUtils.parseObject(reportString, JSONObject.class);
    }
}

