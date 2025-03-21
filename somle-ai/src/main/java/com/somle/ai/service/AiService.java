package com.somle.ai.service;

import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.ai.model.AiName;
import com.somle.ai.model.AiReqVO;
import com.somle.ai.model.AiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class AiService {


    private final String baseUrl = "http://ai.somle.com:55003";
    private final Map<String, String> headers = Map.of(
        "Authorization", "Token 4b9a81992261ad3f01aecd4608beb1e9c9828865"
    );
    private final Integer pageSize = 100;

    public AiReqVO newCreateVO(LocalDate date) {
        var firstSecond = date.atStartOfDay();
        var lastSecond = date.plusDays(1).atStartOfDay().minusSeconds(1);
        return AiReqVO.builder()
            .createTimeAfter(firstSecond)
            .createTimeBefore(lastSecond)
            .limit(pageSize)
            .build();
    }

    public AiReqVO newUpdateVO(LocalDate date) {
        var firstSecond = date.atStartOfDay();
        var lastSecond = date.plusDays(1).atStartOfDay().minusSeconds(1);
        return AiReqVO.builder()
            .updateTimeAfter(firstSecond)
            .updateTimeBefore(lastSecond)
            .limit(pageSize)
            .build();
    }




    public void addName(AiName name) {
        String endUrl = "/api/persons/";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(baseUrl + endUrl)
            .headers(headers)
            .payload(name)
            .build();
        AiName response = WebUtils.sendRequest(request, AiName.class);

    }


    public void addAddress(Object address) {
        String endUrl = "/api/locationsdtl/";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(baseUrl + endUrl)
            .headers(headers)
            .payload(address)
            .build();
        AiResponse response = WebUtils.sendRequest(request, AiResponse.class);

    }


    public Stream<AiResponse> getPersons(AiReqVO vo) {
        return getReponses("/api/persons/", vo);
    }

    public Stream<AiResponse> getAddresses(AiReqVO vo) {
        return getReponses("/api/locationsdtl/", vo);
    }

    public AiResponse getCountries() {
        return getReponse("/api/countriesiso/");
    }

    public AiResponse getCurrencies() {
        return getReponse("/api/currency/");
    }

//    public <T> Stream<T> getNew(String endUrl, LocalDate dataDate, Class<T> objectclass) {
//        LocalDateTime today = dataDate.atStartOfDay();
//        LocalDateTime tomorrow = today.plusDays(1);
//
//        Map<String, String> paramsAdded = Map.of(
//            "create_time_after", today.toString(),
//            "create_time_before", tomorrow.toString(),
//            "limit", "100"
//
//        );
//
//        String urlAdded = WebUtils.urlWithParams(baseUrl + endUrl, paramsAdded);
//
//        Map<String, String> paramsUpdated = Map.of(
//            "create_time_before", today.toString(),
//            "update_time_after", today.toString(),
//            "update_time_before", tomorrow.toString(),
//            "limit", "100"
//
//        );
//
//        String urlUpdated = WebUtils.urlWithParams(baseUrl + endUrl, paramsUpdated);
//
//        return Stream.concat(getResults(urlAdded, objectclass), getResults(urlUpdated, objectclass));
//    }

//    public <T> Stream<T> getResults(String url, Class<T> objectclass) {
//        return getReponses(url).flatMap(n-> n.getResults(objectclass));
//    }

    public Stream<AiResponse> getReponsesFromUrl(String url) {
        log.debug("fetching from url: " + url);
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url)
            .headers(headers)
            .build();
        AiResponse response = WebUtils.sendRequest(request, AiResponse.class);

        log.debug("next url is: " + response.getNext());
        Stream<AiResponse> moreResponse = response.getNext() == null ? Stream.empty() : getReponsesFromUrl(response.getNext());
        return Stream.concat(Stream.of(response), moreResponse);
    }

    public AiResponse getReponse(String endpoint) {
        return getReponsesFromUrl(baseUrl + endpoint).findFirst().get();
    }

    public Stream<AiResponse> getReponses(String endpoint, Object vo) {
        var url = WebUtils.urlWithParams(baseUrl + endpoint, vo);
        return getReponsesFromUrl(url);
    }
}