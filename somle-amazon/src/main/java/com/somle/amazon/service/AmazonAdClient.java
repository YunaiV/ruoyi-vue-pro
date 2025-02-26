package com.somle.amazon.service;

import cn.iocoder.yudao.framework.common.util.general.CoreUtils;
import cn.iocoder.yudao.framework.common.util.json.JSONArray;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.somle.amazon.controller.vo.AmazonAdAccountRespVO;
import com.somle.amazon.controller.vo.AmazonAdProfileRespVO;
import com.somle.amazon.controller.vo.AmazonAdReportReqVO;
import com.somle.amazon.model.AmazonAdAuthDO;
import com.somle.amazon.model.enums.AmazonRegion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@AllArgsConstructor
@Getter
@Setter
public class AmazonAdClient {


    private AmazonAdAuthDO auth;

    private AmazonRegion region;

    private String getEndPoint() {
        return this.region.getAdUrl();
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



    private JSONObject sendRequest(Long profileId, RequestX request) {
        return CoreUtils.retry(ctx -> {
            try(var response = WebUtils.sendRequest(request);){
                validateResponse(profileId, response);
                return WebUtils.parseResponse(response, JSONObject.class);
            }
        });
    }

    @SneakyThrows
    private void validateResponse(Long profileId, Response response) {
        switch (response.code()) {
            case 200:
                break;
            case 401:
                var additionalMessage = profileId == null ? "" : profileId.toString();
                throw new RuntimeException("Unauthorized error, token expired" + additionalMessage);
            case 425:
                throw new RuntimeException("The Request is a duplicate");
            case 429:
                throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Received 429 Too Many Requests. Retrying...");
            default:
                throw new RuntimeException("Http wrong code response: " + response + "\nbody: " + response.body().string());
        }
    }



    public AmazonAdAccountRespVO listAccounts() {
        JSONObject payload = JsonUtilsX.newObject();

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
        JSONObject payload = JsonUtilsX.newObject();

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

    public JSONObject listPortfolios(Long profileId) {
        JSONObject payload = JsonUtilsX.newObject();

        String partialUrl = "/v2/profiles";
        String endpoint = getEndPoint();
        String fullUrl = endpoint + partialUrl;

        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(fullUrl)
            .headers(generateHeaders(profileId))
            .payload(payload)
            .build();
        return sendRequest(profileId, request);
    }

    public Stream<JSONArray> batchCreateAndGetReport(AmazonAdReportReqVO payload) {
        var reportIdMap = listSellerProfiles().stream().collect(Collectors.toMap(
            profile->profile,
            profile->createReport(profile.getProfileId(), payload)
        ));
        // usually take more than 5 mins
        CoreUtils.sleep(300000);
        return reportIdMap.entrySet().stream()
            .map(entry->getReport(entry.getKey().getProfileId(), entry.getValue()));
    }

    public JSONArray createAndGetReport(Long profileId, AmazonAdReportReqVO payload, LocalDate dataDate) {
        var reportId = createReport(profileId, payload);
        return getReport(profileId, reportId);
    }




    // return report id
    @SneakyThrows
    public String createReport(Long profileId, AmazonAdReportReqVO payload) {


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
            var response = sendRequest(profileId, request);
            reportId = response.getString("reportId");
        }
        log.info("Got report ID for profileId: " + profileId);
        return reportId;
    }

    @SneakyThrows
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
            JSONObject responseBody = sendRequest(profileId, request);

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

        var contentString = WebUtils.urlToString(docUrl, "GZIP");

        return JsonUtilsX.parseObject(contentString, JSONArray.class);
    }


    
}
