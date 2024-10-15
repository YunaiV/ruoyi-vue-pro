package com.somle.microsoft.service;

import com.somle.framework.test.core.ut.BaseSpringTest;
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
class MicrosoftServiceTest extends BaseSpringTest {

    @Resource
    MicrosoftService service;

    @MockBean
    MessageChannel dataChannel;


    @Test
    void getToken() {
        var result = service.getPasswordToken();
        log.info(result);
    }




}