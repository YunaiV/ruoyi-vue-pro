package com.somle.amazon.service;

import com.somle.amazon.model.AmazonSeller;
import com.somle.amazon.repository.AmazonAccountRepository;
import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;


@Slf4j
@Import({
    AmazonService.class,
})
class AmazonServiceTest extends BaseSpringTest {
    @Resource
    AmazonService amazonService;

    @Resource
    AmazonAccountRepository accountRepository;


    @Test
    void refreshAuth() {
        amazonService.refreshAuth();
    }

    @Test
    void getAsinReport() {
        var report = amazonService.spClient.getAsinReport("UK", LocalDate.of(2024,8,10));
        log.info(report.toString());
    }

    @Test
    void getAdReport() {
        var report = amazonService.adClient.getAdReport("UK", LocalDate.of(2024,8,10));
        log.info(report.toString());
    }

//    @Test
//    void printAccount1() {
//        log.info(amazonService.adClient.getShops().findFirst().get().getSeller().getAdAccessToken());
//        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
//        updateAccount1();
//        log.info(amazonService.adClient.getShops().findFirst().get().getSeller().getAdAccessToken());
//        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
//    }
//
//    @Test
//    void printAccount2() {
//        var shop = amazonService.adClient.getShops().findFirst().get();
//        log.info(shop.getSeller().getAdAccessToken());
//        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
//        updateAccount2();
//        log.info(shop.getSeller().getAdAccessToken());
//        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
//    }
//
//    void updateAccount1() {
//        var account = accountRepository.findAll().getFirst();
////        log.info(String.valueOf(account.getSellers().size()));
//        for (AmazonSeller seller : account.getSellers()) {
//            seller.setAdAccessToken("updatedToken");
//        }
//        accountRepository.save(account);
//        amazonService.adClient.account = accountRepository.findAll().getFirst();
//    }
//
//    void updateAccount2() {
//        var account = amazonService.adClient.account;
//        for (AmazonSeller seller : account.getSellers()) {
//            seller.setAdAccessToken("updatedToken");
//        }
//        accountRepository.save(account);
//    }
}