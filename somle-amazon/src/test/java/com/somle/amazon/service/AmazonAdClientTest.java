package com.somle.amazon.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.amazon.model.enums.AmazonRegion;
import com.somle.amazon.repository.AmazonAdAuthRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;


@Disabled
@Slf4j
@Import({
})
class AmazonAdClientTest extends SomleBaseDbUnitTest {
    @Resource
    AmazonAdAuthRepository repository;

    private AmazonAdClient client;

    @BeforeEach
    void setUp() {
         client = new AmazonAdClient(repository.findById(1l).get(), AmazonRegion.NA);
    }





    @Test
    void listAccounts() {
        var response = client.listAccounts();
        log.info(response.toString());
    }

    @Test
    void listProfiles() {
        var response = client.listProfiles();
        log.info(response.toString());
    }

    @Test
    void createAdReport() {
        var profileId = client.listProfiles().get(0).getProfileId();
//        var reportId = client.createAdReport(profileId, LocalDate.of(2025,1,20));
//        log.info(reportId);
//        var report = client.getReport(profileId, reportId);
//        log.info(report.toString());
    }

//    @Test
//    void testGetAdReport() {
//    }
//
//    @Test
//    void getReport() {
//    }
}