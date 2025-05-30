package com.somle.lazada.service;

import com.somle.lazada.model.LazadaAccount;
import com.somle.lazada.model.reps.LazadaRefreshTokenResp;
import com.somle.lazada.repository.LazadaAccountRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LazadaService {

    public List<LazadaClient> clients;

    @Resource
    LazadaAccountRepository lazadaAccountRepository;

    @PostConstruct
    public void init() {
        this.clients = getAllClients();
    }

    public List<LazadaClient> getAllClients() {
        List<LazadaAccount> accountList = lazadaAccountRepository.findAll();
        clients = new ArrayList<>();
        for (LazadaAccount lazadaAccount : accountList) {
            clients.add(new LazadaClient(lazadaAccount));
        }
        return clients;
    }

    @Scheduled(cron = "0 0,30 * * * *")
    public void refreshAuths() {
        clients.forEach(client -> {
            LazadaRefreshTokenResp lazadaRefreshTokenResp = client.refreshToken();
            LazadaAccount lazadaAccount = client.getLazadaAccount();
            lazadaAccount.setAccessToken(lazadaRefreshTokenResp.getAccessToken());
            client.setLazadaAccount(lazadaAccount);
            lazadaAccountRepository.save(lazadaAccount);
        });
    }
}