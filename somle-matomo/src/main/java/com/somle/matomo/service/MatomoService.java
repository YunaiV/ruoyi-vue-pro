package com.somle.matomo.service;

import com.somle.framework.common.util.json.JsonUtils;
import com.somle.matomo.model.MatomoVisit;
import com.somle.framework.common.util.web.WebUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.messaging.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.IntStream;

@Slf4j
@Service
public class MatomoService {

    @Autowired
    private MessageChannel dataChannel;


    private final String baseUrl = "https://fitueyes.matomo.cloud";
    private final String token = "062183e6bd55a126e61976a97ad89b60";


    public Stream<MatomoVisit> getVisits(Integer idSite, LocalDate dataDate) {
        Map<String, String> params = Map.of(
            "module","API",
            "method", "Live.getLastVisitsDetails",
            "idSite", idSite.toString(),
            "period", "day",
            "date", dataDate.toString(),
            "format", "json",
            "token_auth", token,
            "filter_limit", "10000"
        );

        var response = WebUtils.postRequest(baseUrl, params, Map.of(), null);
        var responseString = WebUtils.getBodyString(response);

        return JsonUtils.parseArray(responseString, MatomoVisit.class).stream();
    }

    public Stream<MatomoVisit> getVisits(LocalDate dataDate) {
        return IntStream.range(1, 6).boxed().flatMap(i->getVisits(i, dataDate));
    }
    
}
