package com.somle.amazon.service;

import com.somle.amazon.model.AmazonSeller;
import com.somle.amazon.repository.AmazonAccountRepository;
import com.somle.framework.common.util.general.CoreUtils;
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


    static boolean run = true;


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

    @Test
    void concurency1() {
        log.info(amazonService.adClient.getShops().findFirst().get().getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
        updateAccount1();
        log.info(amazonService.adClient.getShops().findFirst().get().getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
    }

    void updateAccount1() {
        var account = accountRepository.findAll().getFirst();
//        log.info(String.valueOf(account.getSellers().size()));
        for (AmazonSeller seller : account.getSellers()) {
            seller.setAdAccessToken("updatedToken");
        }
        accountRepository.save(account);
        amazonService.adClient.account = accountRepository.findAll().getFirst();
    }

    @Test
    void concurency2() {
        var shop = amazonService.adClient.getShops().findFirst().get();
        log.info(shop.getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
        updateAccount2();
        log.info(shop.getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
    }

    @Test
    void concurency3() throws InterruptedException {
        var shop = amazonService.adClient.getShops().findFirst().get();
        log.info(shop.getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
        Thread t = new Thread(()->{
            Integer count = 0;
            while (!shop.getSeller().getAdAccessToken().equals("updatedToken")) {
                count++;
            }
            log.info(count.toString());
        });
        t.start();
        CoreUtils.sleep(1000);
        updateAccount2();
        log.info(shop.getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
        t.join();
    }

    @Test
    void concurency4() throws InterruptedException {
        var shop = amazonService.adClient.getShops().findFirst().get();
        log.info(shop.getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
        Thread t = new Thread(()->{
            int i = 0;
            while (true) {
                i++;
                amazonService.refreshAuth();
                log.info("loop " + String.valueOf(i));
                log.info(shop.getSeller().getAdAccessToken());
                log.info(amazonService.adClient.account.getSellers().getFirst().getAdAccessToken());
            }
        });
        t.start();
        amazonService.adClient.getAllAdReport(LocalDate.now().minusDays(3));
    }

    @Test
    void visibility() throws InterruptedException {
        Thread t = new Thread(()->{
            while (run) {
            }
        });
        t.start();
        CoreUtils.sleep(1000);
        log.info("stop");
        run = false;
        t.join();
    }

    void updateAccount2() {
        var account = amazonService.adClient.account;
        for (AmazonSeller seller : account.getSellers()) {
            seller.setAdAccessToken("updatedToken");
        }
        accountRepository.save(account);
    }
}