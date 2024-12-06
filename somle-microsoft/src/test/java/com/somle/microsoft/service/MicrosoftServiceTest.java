package com.somle.microsoft.service;

import com.somle.framework.test.core.ut.BaseDbUnitTest;
import com.somle.framework.test.core.ut.BaseSpringTest;
import com.somle.microsoft.model.PowerbiAccount;
import com.somle.microsoft.model.PowerbiReportReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Import(MicrosoftService.class)
class MicrosoftServiceTest extends BaseDbUnitTest {

    @Resource
    MicrosoftService service;

    @MockBean
    MessageChannel dataChannel;


    @Test
    void getToken() {
        service.getPasswordToken();
    }

    @Test
    void getReports() {
        service.getReports("380f74e7-ca4f-4512-9f49-8cd92a3c299e");
    }

    @Test
    void getEmbedReport() {
        var result = service.getEmbedReport(
            PowerbiReportReqVO.builder()
                .groupId("380f74e7-ca4f-4512-9f49-8cd92a3c299e")
                .reportId("b7abcd9e-4a23-4af0-9b74-91467574d757")
                .build()
        );
        log.info(result.toString());
    }




}