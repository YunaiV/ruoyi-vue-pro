package com.somle.amazon.service;

import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.model.AmazonErrorList;
import com.somle.amazon.model.AmazonShop;
//import com.somle.amazon.repository.AmazonSellerRepository;
import com.somle.framework.common.util.general.CoreUtils;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.WebUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.persistence.*;

import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    public Stream<JSONObject> getAllAsinReport(LocalDate dataDate) {
        return getShops().map(shop->getAsinReport(shop, dataDate));
    }

    @Transactional(readOnly = true)
    public JSONObject getAsinReport(String countryCode, LocalDate dataDate) {
        return getAsinReport(getShop(countryCode), dataDate);
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
                    var report = WebUtils.parseResponse(response, Report.class);
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
                    throw new RuntimeException("Unknown response code");
            }
        }
        log.info("Got report ID: {}", reportId);

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
                    result.put("Message", "No data returned, get report fail.");
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
        result = WebUtils.urlToDict(docUrl, "gzip", JSONObject.class);


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