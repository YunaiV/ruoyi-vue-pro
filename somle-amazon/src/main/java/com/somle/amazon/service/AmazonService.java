package com.somle.amazon.service;

import com.alibaba.fastjson2.JSONObject;
import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.model.AmazonSeller;
import com.somle.amazon.repository.AmazonAccountRepository;
import com.somle.amazon.repository.AmazonShopRepository;
import com.somle.util.Util;

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
        JSONObject body = new JSONObject();
        body.put("grant_type", "refresh_token");
        account = accountRepository.findAll().get(0);
        for (AmazonSeller seller : account.getSellers()) {
    
            try {
                body.put("client_id", account.getSpClientId());
                body.put("client_secret", account.getSpClientSecret());
                body.put("refresh_token", seller.getSpRefreshToken());
                // JSONObject response = restTemplate.postForObject(authUrl, body, JSONObject.class);
                JSONObject response = Util.postRequest(authUrl, Map.of(), Map.of(), body, JSONObject.class);
                seller.setSpAccessToken(response.getString("access_token"));
                accountRepository.save(account);
            } catch (Exception e) {
                log.error("Failed to set sp access token", e);
                throw new RuntimeException("Failed to get access token", e);
            }

            try {
                body.put("client_id", account.getSpClientId());
                body.put("client_secret", account.getAdClientSecret());
                body.put("refresh_token", seller.getAdRefreshToken());
                JSONObject response = Util.postRequest(authUrl, Map.of(), Map.of(), body, JSONObject.class);
                // JSONObject response = restTemplate.postForObject(authUrl, body, JSONObject.class);
                seller.setAdAccessToken(response.getString("access_token"));
                accountRepository.save(account);
            } catch (Exception e) {
                log.error("Failed to set ad access token", e);
                throw new RuntimeException("Failed to get access token", e);
            }
        }
    }



}
