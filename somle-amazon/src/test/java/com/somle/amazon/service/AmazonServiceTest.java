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
    void generateAccessToken() {
        var clientId = amazonService.accounts.get(0).getAdClientId();
        var clientSecret = amazonService.accounts.get(0).getAdClientSecret();
        var code = "";
        var response = amazonService.generateAccessToken(clientId, clientSecret, code);
        log.info(response);
    }



    @Test
    void getAdReport() {
        var shop = amazonService.shopRepository.findByCountryCode("DE");
        var reportId = amazonService.adClient.createAdReport(shop, LocalDate.of(2024,8,25));
        log.info(reportId.toString());

        var report = amazonService.adClient.getReport(shop, reportId);
        log.info(report.toString());
    }

    @Test
    void getAllAdReport() {

        var report = amazonService.adClient.getAllAdReport(LocalDate.of(2024,10,23)).toList();
        log.info(report.toString());
    }


    @Test
    void concurency1() {
        log.info(amazonService.adClient.getShops().findFirst().get().getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().get(0).getAdAccessToken());
        updateAccount1();
        log.info(amazonService.adClient.getShops().findFirst().get().getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().get(0).getAdAccessToken());
    }

    void updateAccount1() {
        var account = accountRepository.findAll().get(0);
//        log.info(String.valueOf(account.getSellers().size()));
        for (AmazonSeller seller : account.getSellers()) {
            seller.setAdAccessToken("updatedToken");
        }
        accountRepository.save(account);
        amazonService.adClient.account = accountRepository.findAll().get(0);
    }

    @Test
    void concurency2() {
        var shop = amazonService.adClient.getShops().findFirst().get();
        log.info(shop.getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().get(0).getAdAccessToken());
        updateAccount2();
        log.info(shop.getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().get(0).getAdAccessToken());
    }

    @Test
    void concurency3() throws InterruptedException {
        var shop = amazonService.adClient.getShops().findFirst().get();
        log.info(shop.getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().get(0).getAdAccessToken());
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
        log.info(amazonService.adClient.account.getSellers().get(0).getAdAccessToken());
        t.join();
    }

    @Test
    void concurency4() throws InterruptedException {
        var shop = amazonService.adClient.getShops().findFirst().get();
        log.info(shop.getSeller().getAdAccessToken());
        log.info(amazonService.adClient.account.getSellers().get(0).getAdAccessToken());
        Thread t = new Thread(()->{
            int i = 0;
            while (true) {
                i++;
                amazonService.refreshAuth();
                log.info("loop " + String.valueOf(i));
                log.info(shop.getSeller().getAdAccessToken());
                log.info(amazonService.adClient.account.getSellers().get(0).getAdAccessToken());
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