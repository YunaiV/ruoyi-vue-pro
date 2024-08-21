package com.somle.ai.service;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.somle.ai.model.AiName;
import com.somle.ai.model.AiResponse;
import com.somle.erp.model.ErpCountry;
import com.somle.erp.model.ErpCurrency;
import com.somle.util.Util;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.messaging.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class AiService {

    @Autowired
    private MessageChannel dataChannel;

    private final String baseUrl = "http://ai.somle.com:55003";
    private final Map<String, String> headers = Map.of(
        "Authorization", "Token 4b9a81992261ad3f01aecd4608beb1e9c9828865"
    );




    public void addName(AiName name) {
        String endUrl = "/api/persons/";
        AiName response = Util.postRequest(baseUrl + endUrl, Map.of(), headers, name, AiName.class);

    }


    public void addAddress(Object address) {
        String endUrl = "/api/locationsdtl/";

        AiResponse response = Util.postRequest(baseUrl + endUrl, Map.of(), headers, address, AiResponse.class);

    }
    

    public Stream<AiName> getNames(LocalDate dataDate) {
        return getNew("/api/persons/", dataDate, AiName.class);
    }

    public Stream<String> getAddresses(LocalDate dataDate) {
        return getNew("/api/locationsdtl/", dataDate, String.class);
    }

    public <T> Stream<T> getNew(String endUrl, LocalDate dataDate, Class<T> objectclass) {
        LocalDateTime today = dataDate.atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);

        Map<String, String> paramsAdded = Map.of(
            "create_time_after", today.toString(),
            "create_time_before", tomorrow.toString(),
            "limit", "100"

        );

        String urlAdded = Util.urlWithParams(baseUrl + endUrl, paramsAdded);

        Map<String, String> paramsUpdated = Map.of(
            "create_time_before", today.toString(),
            "update_time_after", today.toString(),
            "update_time_before", tomorrow.toString(),
            "limit", "100"

        );

        String urlUpdated = Util.urlWithParams(baseUrl + endUrl, paramsUpdated);

        return Stream.concat(getResults(urlAdded, objectclass), getResults(urlUpdated, objectclass));
    }

    public <T> Stream<T> getResults(String url, Class<T> objectclass) {
        return getReponses(url).flatMap(n->n.getResults(objectclass));
    }

    public Stream<AiResponse> getReponses(String url) {
        log.debug("fetching from url: " + url);
        AiResponse response = Util.getRequest(url, Map.of(), headers, AiResponse.class);

        log.debug("next url is: " + response.getNext());
        Stream<AiResponse> moreResponse = response.getNext() == null ? Stream.empty() : getReponses(response.getNext());
        return Stream.concat(Stream.of(response), moreResponse);
    }

    public Stream<ErpCountry> getCountries() {
        String endUrl = "/api/countriesiso/";
        String response = Util.getRequest(baseUrl + endUrl, Map.of(), headers, String.class);
        return JsonUtils.parseArray(response, ErpCountry.class).stream();
    }

    public Stream<ErpCurrency> getCurrencies() {
        String endUrl = "/api/currency/";
        String response = Util.getRequest(baseUrl + endUrl, Map.of(), headers, String.class);
        return JsonUtils.parseArray(response, ErpCurrency.class).stream();
    }
}