package com.somle.cdiscount.service;

import com.somle.cdiscount.repository.CdiscountTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CdiscountService {

    public CdiscountClient client;


    @Resource
    public CdiscountTokenRepository tokenRepository;

    @PostConstruct
    public void init() {
        client = new CdiscountClient(tokenRepository.findAll().get(0));
        refreshAuths();
    }

    //每两小时刷新一次token
    @Scheduled(cron = "0 0 */2 * * ?")
    public boolean refreshAuths() {
        boolean success = true;
        client.refreshToken();
        tokenRepository.save(client.getToken());
        log.info("token saved successfully");
        return success;
    }
}
