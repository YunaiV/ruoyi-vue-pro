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



    @Scheduled(cron = "0 20 1 * * *") // Executes at 1:20 AM every day
    // @Scheduled(fixedDelay = 999999999, initialDelay = 1000)
    public void dataCollect() {
        LocalDate scheduleDate = LocalDate.now();
        dataCollect(scheduleDate);
    }

    
    public void dataCollect(LocalDate startScheduleDate, LocalDate endScheduleDate) {
        LocalDate scheduleDate = startScheduleDate;
        while (! scheduleDate.isAfter(endScheduleDate)) {
            dataCollect(scheduleDate);
            scheduleDate = scheduleDate.plusDays(1);
        }
    }

    public void dataCollect(LocalDate scheduleDate) {
        LocalDate today = scheduleDate;
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        // getVisits(today)
        //     .forEach(visit -> {
        //         OssData data = OssData.builder()
        //             .database("matomo")
        //             .tableName("visit")
        //             .syncType("inc")
        //             .requestTimestamp(System.currentTimeMillis())
        //             .folderDate(today)
        //             .content(visit)
        //             .headers(null)
        //             .build();
        //         dataChannel.send(MessageBuilder.withPayload(data).build());
        //     });



    }

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

        String response = WebUtils.postRequest(baseUrl, params, Map.of(), null, String.class);

        return JsonUtils.parseArray(response, MatomoVisit.class).stream();
    }

    public Stream<MatomoVisit> getVisits(LocalDate dataDate) {
        return IntStream.range(1, 6).boxed().flatMap(i->getVisits(i, dataDate));
    }
    
}
