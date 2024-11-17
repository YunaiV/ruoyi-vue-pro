package com.somle.matomo.service;

import com.somle.framework.common.util.json.JsonUtils;
import com.somle.matomo.model.MatomoMethodVO;
import com.somle.matomo.model.MatomoTokenVO;
import com.somle.matomo.model.MatomoVisit;
import com.somle.framework.common.util.web.WebUtils;

import com.somle.matomo.model.MatomoVisitReqVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;
import org.springframework.messaging.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.stream.Stream;
import java.util.stream.IntStream;

@Slf4j
@Service
public class MatomoService {

    @Autowired
    private MessageChannel dataChannel;


    private final String baseUrl = "https://fitueyes.matomo.cloud";
    private final String token = "062183e6bd55a126e61976a97ad89b60";

    public Stream<MatomoVisit> getVisits(MatomoVisitReqVO vo) {
        var methodVO = MatomoMethodVO.builder()
            .module("API")
            .method("Live.getLastVisitsDetails")
            .build();

        var responseString = getResponse(methodVO, vo);

        return JsonUtils.parseArray(responseString, MatomoVisit.class).stream();

    }

    @SneakyThrows
    public String getResponse(MatomoMethodVO methodVO, Object vo) {
        var tokenVO = MatomoTokenVO.builder()
            .tokenAuth(token)
            .build();

        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        var url = WebUtils.urlWithParams(baseUrl, tokenVO);
        url = WebUtils.urlWithParams(url, methodVO);
        url = WebUtils.urlWithParams(url, vo);
        var request = new Request.Builder()
            .url(url)
            .method("GET", null)
            .build();
        var response = client.newCall(request).execute();
        var responseString = response.body().string();

        return responseString;
    }
    
}
