package com.somle.amazon.service;

import com.alibaba.fastjson2.JSONObject;
import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.model.AmazonErrorList;
import com.somle.amazon.model.AmazonShop;
//import com.somle.amazon.repository.AmazonSellerRepository;
import com.somle.util.Util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Stream;


@Slf4j
@AllArgsConstructor
public class AmazonSpClient {

    // private List<AmazonShop> shops;

    public AmazonAccount account;




    @Transactional(readOnly = true)
    public Stream<AmazonShop> getShops() {
        return account.getSellers().stream().flatMap(seller->seller.getShops().stream());
    }

    @Transactional(readOnly = true)
    public AmazonShop getShop(String countryCode) {
        return getShops().filter(shop->shop.getCountry().getCode().equals(countryCode)).findFirst().get();
    }

    @Transactional(readOnly = true)
    public Stream<JSONObject> getAllAsinReport(LocalDate dataDate) {
        return getShops().map(shop->getAsinReport(shop, dataDate));
    }

    @Transactional(readOnly = true)
    public JSONObject getAsinReport(String countryCode, LocalDate dataDate) {
        return getAsinReport(getShop(countryCode), dataDate);
    }

    @Transactional(readOnly = true)
    public JSONObject getAsinReport(AmazonShop shop, LocalDate dataDate) {
        JSONObject options = new JSONObject();
        
        // Add reportType
        options.put("reportType", "GET_SALES_AND_TRAFFIC_REPORT");

        // Add marketplaceIds
        options.put("marketplaceIds", new String[] {shop.getCountry().getMarketplaceId()});

        // Create and add reportOptions
        // Create and add reportOptions
        JSONObject reportOptions = new JSONObject();
        reportOptions.put("asinGranularity", "CHILD");
        reportOptions.put("dateGranularity", "DAY");
        options.put("reportOptions", reportOptions);
        return getReport(shop, options, dataDate);
    }

    @Transactional(readOnly = true)
    public JSONObject getReport(AmazonShop shop, JSONObject payload, LocalDate dataDate) {
        var countryCode = shop.getCountry().getCode();
        log.info("get report");
        int RETENTION_DAYS = 720;
        DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String endPoint = getShop(countryCode).getCountry().getRegion().getSpEndPoint();
        String partialUrl = "/reports/2021-06-30/reports";
        String fullUrl = endPoint + partialUrl;
        var headers = Map.of("x-amz-access-token", getShop(countryCode).getSeller().getSpAccessToken());
        JSONObject result = new JSONObject();
        payload.put("dataStartTime", dataDate.format(DATE_FORMAT));
        payload.put("dataEndTime", dataDate.format(DATE_FORMAT));

        // Create report
        String reportId = null;
        while (reportId == null) {
            log.info("creating report");
            var response = Util.postRequest(fullUrl, Map.of(), headers, payload);
            switch (response.code()) {
                case 202:
                    var report = Util.parseResponse(response, Report.class);
                    reportId = report.getReportId();
                    break;
                case 403:
                    var error = Util.parseResponse(response, AmazonErrorList.class);
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
                    throw new RuntimeException("Unknown response code");
            }
        }
        log.info("Got report ID: {}", reportId);

        // Check report status and get document ID
        String status = null;
        String reportStatusUrl = endPoint + "/reports/2021-06-30/reports/" + reportId;
        String docId = null;
        while (!"DONE".equals(status)) {
            try {
                log.info("requesting for document id");
                // ResponseEntity<JSONObject> response = restTemplate.exchange(reportStatusUrl, HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class);
                // JSONObject responseBody = response.getBody();
                JSONObject response = Util.getRequest(reportStatusUrl, Map.of(), headers, JSONObject.class);
                JSONObject responseBody = response;
                status = responseBody.getString("processingStatus");
                log.info(status);
                docId = responseBody.getString("reportDocumentId");
                if ("CANCELLED".equals(status)) {
                    result.put("Message", "No data returned, get report fail.");
                    return result;
                }
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

        // Use document ID to get download URL
        String docUrl = null;
        String documentUrl = endPoint + "/reports/2021-06-30/documents/" + docId;
        while (docUrl == null) {
            try {
                // ResponseEntity<JSONObject> response = restTemplate.exchange(documentUrl, HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class);
                // JSONObject responseBody = response.getBody();
                JSONObject response = Util.getRequest(documentUrl, Map.of(), headers, JSONObject.class);
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
        result = Util.urlToDict(docUrl, "gzip", JSONObject.class);
        // requestDict = {'headers': headers, 'body': payload};
        // return requestDict, contentDict;

        return result;
        
    }
}

@Data
class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportId;
    private String processingStatus;
    private String reportDocumentId;
    private String url;
}