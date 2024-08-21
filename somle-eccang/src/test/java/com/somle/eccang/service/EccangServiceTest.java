//package com.somle.eccang.service;
//
//import com.somle.eccang.repository.EccangTokenRepository;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.*;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.test.context.ActiveProfiles;
//
//
//@Slf4j
//@SpringBootTest(classes = EccangServiceTest.Application.class)
//@ActiveProfiles("unit-test")
//@Import({
//    EccangService.class,
//})
//public class EccangServiceTest {
//
//    @Resource
//    EccangService eccangService;
//
//    @MockBean
//    EccangTokenRepository eccangTokenRepository;
//
//    @MockBean(name="dataChannel")
//    MessageChannel dataChannel;
//
//    @MockBean(name="saleChannel")
//    MessageChannel saleChannel;
//
//
//    @Test
//    void list() {
//        var result = eccangService.list("getWarehouse");
//        System.out.println(result.toString());
//    }
//
//    @Test
//    void post() {
//    }
//
//
//    public static class Application {
//    }
//}