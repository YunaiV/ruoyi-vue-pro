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
    void getEmbedReport() {
        var result = service.getEmbedReport(
            PowerbiReportReqVO.builder()
                .groupId("992affd7-1e95-4213-bb80-758a9d1dbe86")
                .reportId("e5e3fbe6-bd1b-479a-b417-fac868bcbd4a")
                .build()
        );
        log.info(result.toString());
    }




}