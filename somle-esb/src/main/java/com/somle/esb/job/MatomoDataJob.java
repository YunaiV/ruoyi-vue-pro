package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.matomo.model.MatomoMethodVO;
import com.somle.matomo.model.MatomoVisitReqVO;
import com.somle.matomo.service.MatomoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Component
public class MatomoDataJob extends DataJob {
    @Autowired
    EsbService service;

    @Autowired
    MatomoService matomoService;

    final String DATABASE = Domain.MATOMO.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);



        IntStream.range(1, 7).boxed().forEach(idSite -> {
            var methodVO = MatomoMethodVO.builder()
                .module("API")
                .method("Live.getLastVisitsDetails")
                .build();

            var reqVO = MatomoVisitReqVO.builder()
                .idSite(String.valueOf(idSite))
                .format("json")
                .period("day")
                .date(beforeYesterday)
                .filterLimit(1000)
                .filterOffset(0)
                .build();

            var results = StreamX.iterate(
                matomoService.getResponse(methodVO, reqVO),
                response -> !response.isEmpty(),
                response -> {
                    reqVO.setFilterOffset(reqVO.getFilterOffset() + reqVO.getFilterLimit());
                    return matomoService.getResponse(methodVO, reqVO);
                }
            );

            results.forEach(result->{
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("visit")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(result)
                    .headers(null)
                    .build();
                service.send(data);
            });
        });
        return "data upload success";
    }
}