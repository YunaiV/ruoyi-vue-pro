package com.somle.cdiscount.service;

import com.somle.cdiscount.model.CdiscountToken;
import com.somle.cdiscount.repository.CdiscountTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CdiscountService {

    public List<CdiscountClient> clients;


    @Resource
    public CdiscountTokenRepository tokenRepository;

    @PostConstruct
    public void init() {
        clients = tokenRepository.findAll().stream().map(CdiscountClient::new).toList();
    }

    //每两小时刷新一次token
    @Scheduled(cron = "0 0 */2 * * ?")
    public boolean refreshAuths() {
        boolean success = true;
        clients.forEach(client -> {
            client.refreshToken();
            CdiscountToken token = client.getToken();
            tokenRepository.save(token);
        });
        log.info("token saved successfully");
        return success;
    }
}
