package com.somle.matomo.service;

import com.fasterxml.jackson.databind.JsonNode;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.somle.matomo.model.MatomoMethodVO;
import com.somle.matomo.model.MatomoTokenVO;
import com.somle.matomo.model.MatomoVisit;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;

import com.somle.matomo.model.MatomoVisitReqVO;
import com.somle.matomo.repository.MatomoTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;
import org.springframework.messaging.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

@Slf4j
@Service
public class MatomoService {

    @Autowired
    private MessageChannel dataChannel;

    @Resource
    private MatomoTokenRepository matomoTokenRepository;


    private static final String BASE_URL = "https://fitueyes.matomo.cloud";
    private String token;


    @PostConstruct
    public void init() {
        token = matomoTokenRepository.findAll().get(0).getToken();
    }

    public Stream<MatomoVisit> getVisits(MatomoVisitReqVO vo) {
        var methodVO = MatomoMethodVO.builder()
            .module("API")
            .method("Live.getLastVisitsDetails")
            .build();

        var responseString = getResponse(methodVO, vo);

        return JsonUtilsX.parseArray(responseString, MatomoVisit.class).stream();

    }

    @SneakyThrows
    public JsonNode getResponse(MatomoMethodVO methodVO, Object vo) {
        var tokenVO = MatomoTokenVO.builder()
            .tokenAuth(token)
            .build();

        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        var url = WebUtils.urlWithParams(BASE_URL, tokenVO);
        url = WebUtils.urlWithParams(url, methodVO);
        url = WebUtils.urlWithParams(url, vo);
        log.info("url:{}", url);
        var request = new Request.Builder()
            .url(url)
            .method("GET", null)
            .build();
        var response = client.newCall(request).execute();
        var responseString = response.body().string();
        var result = JsonUtilsX.parseJson(responseString);

        return result;
    }
    
}
