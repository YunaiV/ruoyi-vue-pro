package com.somle.amazon.service;

import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.controller.vo.AmazonAuthReqVO;
import com.somle.amazon.controller.vo.AmazonAuthRespVO;
import com.somle.amazon.model.AmazonSeller;
import com.somle.amazon.repository.AmazonAccountRepository;
import com.somle.amazon.repository.AmazonShopRepository;
import com.somle.framework.common.util.web.RequestX;
import com.somle.framework.common.util.web.WebUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AmazonService {

    public List<AmazonAccount> accounts;

    public AmazonAccount account;

    public String authUrl = "https://api.amazon.com/auth/o2/token";



    public AmazonSpClient spClient;


    @Autowired
    AmazonShopRepository shopRepository;


    @Autowired
    AmazonAccountRepository accountRepository;

//    @Resource
//    AmazonAdService adService;


    @PostConstruct
    public void init() {
        accounts = accountRepository.findAll();
        account = accounts.get(0);
        spClient = new AmazonSpClient(accounts.get(0));
//        adClient = new AmazonAdClient(accounts.get(0));
    }



    // public void updateShopList() {
    //     // shopList = shopRepository.findAll().stream().map(shop->shop.getCountryCode()).toList();
    //     shopList = shopRepository.findAll();
    // }
//    @Scheduled(fixedDelay = 1800000, initialDelay = 1000)
    @Scheduled(cron = "0 0,30 * * * *")
    public void refreshAuth() {
        accounts.stream()
            .forEach(account -> {
                for (AmazonSeller seller : account.getSellers()) {
                    seller.setSpExpireTime(LocalDateTime.now().plusSeconds(3600));
                    seller.setSpAccessToken(
                        refreshAccessToken(
                            account.getSpClientId(),
                            account.getSpClientSecret(),
                            seller.getSpRefreshToken()
                        )
                    );

                    seller.setAdExpireTime(LocalDateTime.now().plusSeconds(3600));
                    seller.setAdAccessToken(
                        refreshAccessToken(
                            account.getAdClientId(),
                            account.getAdClientSecret(),
                            seller.getAdRefreshToken()
                        )
                    );
                }
                accountRepository.save(account);
            });
    }

    public String generateAccessToken(String clientId, String clientSecret, String code) {
        AmazonAuthReqVO reqVO = AmazonAuthReqVO.builder()
            .grantType("authorization_code")
            .code(code)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .redirectUri("https://www.amazon.com/")
            .build();
        return authorize(reqVO);
    }

    public String refreshAccessToken(String clientId, String clientSecret, String refreshToken) {
        AmazonAuthReqVO reqVO = AmazonAuthReqVO.builder()
            .grantType("refresh_token")
            .clientId(clientId)
            .clientSecret(clientSecret)
            .refreshToken(refreshToken)
            .build();
        return authorize(reqVO);
    }

    private String authorize(AmazonAuthReqVO reqVO) {
        try {
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(authUrl)
                .payload(reqVO)
                .build();
            var response = WebUtils.sendRequest(request, AmazonAuthRespVO.class);
            var accessToken = response.getAccessToken();
            return accessToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get access token", e);
        }
    }




}
