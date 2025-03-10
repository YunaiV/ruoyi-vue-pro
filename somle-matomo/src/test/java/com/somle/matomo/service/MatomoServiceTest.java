package com.somle.matomo.service;


import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import com.somle.matomo.model.MatomoMethodVO;
import com.somle.matomo.model.MatomoVisitReqVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@Slf4j
class MatomoServiceTest extends BaseMockitoUnitTest {

    MatomoService service = new MatomoService();

    @Test
    void getVisits() {
        var date = LocalDate.of(2024, 9, 24);
        var methodVO = MatomoMethodVO.builder()
            .module("API")
            .method("Live.getLastVisitsDetails")
            .build();
        var reqVO = MatomoVisitReqVO.builder()
            .idSite(String.valueOf(3))
            .format("json")
            .period("day")
            .date(date)
            .filterLimit(1000)
            .filterOffset(0)
            .build();

        var results = StreamX.iterate(
            service.getResponse(methodVO, reqVO),
            response -> !response.isEmpty(),
            response -> {
                reqVO.setFilterOffset(reqVO.getFilterOffset() + reqVO.getFilterLimit());
                return service.getResponse(methodVO, reqVO);
            }
        );
        var resultList = results.toList();
        log.info(resultList.toString());
        log.info("results count: " + resultList.size());
    }
}