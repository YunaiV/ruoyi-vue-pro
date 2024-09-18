package com.somle.ai.service;

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
@Import(AiService.class)
class AiServiceTest extends BaseSpringTest {

    @Resource
    AiService service;

    @MockBean
    MessageChannel dataChannel;

//    @org.junit.jupiter.api.Test
//    void addName() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void addAddress() {
//    }

    @Test
    void getNames() {
        var result = service.getNames(LocalDate.of(2024,8,21));
        log.info(result.toList().toString());
    }

    @Test
    void getAddresses() {
        var result = service.getAddresses(LocalDate.of(2024,8,21));
        log.info(result.toList().toString());
    }

    @Test
    void getCountries() {
        var result = service.getCountries();
        log.info(result.toList().toString());
    }

    @Test
    void getCurrencies() {
        var result = service.getCurrencies();
        log.info(result.toList().toString());
    }


//    @org.junit.jupiter.api.Test
//    void getAddresses() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void getNew() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void getResults() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void getReponses() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void getCountries() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void getCurrencies() {
//    }
}