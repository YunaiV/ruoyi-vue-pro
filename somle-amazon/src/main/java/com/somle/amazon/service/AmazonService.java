package com.somle.amazon.service;

import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.model.AmazonSeller;
import com.somle.amazon.repository.AmazonAccountRepository;
import com.somle.amazon.repository.AmazonShopRepository;
import com.somle.framework.common.util.web.WebUtils;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class AmazonService {

    private AmazonAccount account;

    public String authUrl = "https://api.amazon.com/auth/o2/token";



    public AmazonSpClient spClient;
    public AmazonAdClient adClient;

    // @Getter
    // public List<AmazonShop> shopList;


    @Autowired
    AmazonShopRepository shopRepository;

//    @Autowired
//    AmazonSellerRepository sellerRepository;

    @Autowired
    AmazonAccountRepository accountRepository;



    @PostConstruct
    public void init() {
        account = accountRepository.findAll().get(0);
        spClient = new AmazonSpClient(account);
        adClient = new AmazonAdClient(account);
    }



    // public void updateShopList() {
    //     // shopList = shopRepository.findAll().stream().map(shop->shop.getCountryCode()).toList();
    //     shopList = shopRepository.findAll();
    // }
    @Scheduled(fixedDelay = 1800000, initialDelay = 1000)
    public void refreshAuth() {
        JSONObject body = JsonUtils.newObject();
        body.put("grant_type", "refresh_token");
        account = accountRepository.findAll().get(0);
        for (AmazonSeller seller : account.getSellers()) {
            seller.setSpAccessToken(
                refreshAccessToken(
                    account.getSpClientId(),
                    account.getSpClientSecret(),
                    seller.getSpRefreshToken()
                )
            );

            seller.setAdAccessToken(
                refreshAccessToken(
                    account.getAdClientId(),
                    account.getAdClientSecret(),
                    seller.getAdRefreshToken()
                )
            );
        }
        accountRepository.save(account);
        spClient.setAccount(account);
        adClient.setAccount(account);
    }

    public String refreshAccessToken(String clientId, String clientSecret, String refreshToken) {
        JSONObject body = JsonUtils.newObject();
        body.put("grant_type", "refresh_token");
        try {
            body.put("client_id", clientId);
            body.put("client_secret", clientSecret);
            body.put("refresh_token", refreshToken);
            JSONObject response = WebUtils.postRequest(authUrl, Map.of(), Map.of(), body, JSONObject.class);
            var accessToken = response.getString("access_token");
            return accessToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get access token", e);
        }
    }



}
