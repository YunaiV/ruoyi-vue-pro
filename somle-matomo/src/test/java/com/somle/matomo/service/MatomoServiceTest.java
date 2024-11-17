package com.somle.matomo.service;

import com.somle.framework.test.core.ut.BaseMockitoUnitTest;
import com.somle.matomo.model.MatomoVisitReqVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class MatomoServiceTest extends BaseMockitoUnitTest {

    MatomoService service = new MatomoService();

    @Test
    void getVisits() {
        var date = LocalDate.of(2024, 9, 24);
        var vo = MatomoVisitReqVO.builder()
            .idSite(String.valueOf(3))
            .format("json")
            .period("day")
            .date(date)
            .filterLimit(1000)
            .filterOffset(0)
            .build();
        var result = service.getVisits(vo);
        log.info(result.toList().toString());
    }


}