//package com.somle.esb.service;
//
//import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.annotation.Import;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//
//@Slf4j
//@Import(AliyunService.class)
//class AliyunServiceTest extends SomleBaseDbUnitTest {
//    @Resource
//    AliyunService aliyunService;
//
//    @BeforeEach
//    void setUp() {
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    @Test
//    void storeOss() {
//    }
//
//    @Test
//    void downloadOssFileToMemory() throws IOException {
//        byte[] bytes;
//        try (InputStream inputStream = aliyunService.downloadOssFileToStream("template-word/采购合同模板-wdy.docx")) {
//            bytes = inputStream.readAllBytes();
//        }
//        log.info("bytes:{}", bytes);
//        //大小
//        log.info("bytes.length:{}", bytes.length);
//    }
//
//    @Test
//    void getOssFileByPath() {
//        List<String> ossFileByPath = aliyunService.getOssFileByPath("template-word");
//        log.info("ossFileByPath:{}", ossFileByPath);
//    }
//}