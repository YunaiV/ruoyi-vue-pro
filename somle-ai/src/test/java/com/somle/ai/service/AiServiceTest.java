package com.somle.ai.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringTest;

import java.time.LocalDate;

@Disabled
@Slf4j
@Import(AiService.class)
class AiServiceTest extends SomleBaseSpringTest {

    @Resource
    AiService service;

    @MockBean
    MessageChannel dataChannel;

    @Test
    void getPersons() {
        var vo = service.newCreateVO(LocalDate.of(2024,6,19));
        var result = service.getPersons(vo);
        log.info(result.toList().toString());
    }

    @Test
    void getAddresses() {
        var vo = service.newCreateVO(LocalDate.of(2024,6,19));
        var result = service.getAddresses(vo);
        log.info(result.toList().toString());
    }

    @Test
    void getCountries() {
        var result = service.getCountries();
        log.info(result.toString());
    }

    @Test
    void getCurrencies() {
        var result = service.getCurrencies();
        log.info(result.toString());
    }
}