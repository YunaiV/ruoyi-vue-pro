package com.somle.shopee.service;


import com.somle.shopee.model.ShopeeAccount;
import com.somle.shopee.repository.ShopeeAccountRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class ShopeeService {

    public List<ShopeeClient> clients;
    @Resource
    private ShopeeAccountRepository shopeeAccountRepository;

    @PostConstruct
    public void init() {
        clients = shopeeAccountRepository.findAll().stream().map(ShopeeClient::new).toList();
    }

    @Scheduled(cron = "0 0,30 * * * *")
    public void refreshAuths() {
        for (ShopeeClient client : clients) {
            ShopeeAccount account = client.getAccount();
            String accessToken = client.getAccessTokenByRefreshToken(account.getRefreshToken(), account.getPartnerId(), account.getPartnerKey(), account.getShopId());
            account.setAccessToken(accessToken);
            client.setAccount(account);
            shopeeAccountRepository.save(account);
        }
    }


}