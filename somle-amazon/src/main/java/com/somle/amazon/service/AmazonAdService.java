package com.somle.amazon.service;

import com.somle.amazon.model.AmazonAdAuthDO;
import com.somle.amazon.model.enums.AmazonRegion;
import com.somle.amazon.repository.AmazonAdAuthRepository;
import com.somle.amazon.repository.AmazonAdClientRepository;
import com.somle.framework.common.util.web.WebUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AmazonAdService {
    @Resource
    AmazonService amazonService;

    @Resource
    AmazonAdAuthRepository authRepository;

    @Resource
    AmazonAdClientRepository clientRepository;

    public List<AmazonAdClient> clients;

    public final String REDIRECT_URI = "https://somle.com";

    @PostConstruct
    public void init() {
        clients = authRepository.findAll().stream()
            .map(AmazonAdClient::new)
            .toList();
    }

    public String getAuthUrl() {
        var clientDO = clientRepository.findAll().get(0);
        return WebUtils.urlWithParams(
            "https://www.amazon.com/ap/oa",
            Map.of(
                "client_id",clientDO.getId(),
                "scope","advertising::campaign_management",
                "response_type","code",
                "redirect_uri",REDIRECT_URI
            )
        );
    }

    public Long createAuth(String code, AmazonRegion region) {
        var clientDO = clientRepository.findAll().get(0);
        var authDO = new AmazonAdAuthDO();
        var response = amazonService.generateAccessToken(
            clientDO.getId(),
            clientDO.getSecret(),
            code,
            REDIRECT_URI
        );
        authDO.setClientId(clientDO.getId());
        authDO.setRefreshToken(response.getRefreshToken());
        authDO.setRegionCode(region.getCode());
        authRepository.save(authDO);
        clients.add(new AmazonAdClient(authDO));
        refreshAuth();
        return authDO.getId();
    }



    @Scheduled(cron = "0 0,30 * * * *")
    public void refreshAuth() {
        clients.stream()
            .forEach(client -> {
                var auth = client.getAuth();
                var newAccessToken = amazonService.refreshAccessToken(
                    auth.getClientId(),
                    clientRepository.findById(auth.getClientId()).get().getSecret(),
                    auth.getRefreshToken()
                );
                auth.setAccessToken(newAccessToken);
                client.setAuth(auth);
                authRepository.save(auth);
            });
    }

}
