package com.somle.amazon.service;

import com.somle.amazon.model.AmazonAdAuthDO;
import com.somle.amazon.model.enums.AmazonRegion;
import com.somle.amazon.repository.AmazonAdAuthRepository;
import com.somle.amazon.repository.AmazonAdClientRepository;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class AmazonAdService {
    @Resource
    AmazonService amazonService;

    @Resource
    AmazonAdAuthRepository authRepository;

    List<AmazonAdAuthDO> auths;

    @Resource
    AmazonAdClientRepository clientRepository;


    public final String REDIRECT_URI = "https://somle.com";

    @PostConstruct
    public void init() {
//        clients = new ArrayList<>();
//        for (var auth : authRepository.findAll()) {
//            clients.addAll(createClients(auth));
//        }
        auths = new ArrayList<>();
        for (var auth : authRepository.findAll()) {
            auths.add(auth);
        }

    }

    public List<AmazonAdClient> createAllClients() {
        return auths.stream()
            .flatMap(auth -> createClients(auth).stream())
            .toList();
    }


    private List<AmazonAdClient> createClients(AmazonAdAuthDO authDO) {
        return Stream.of(AmazonRegion.values())
            .map(region -> new AmazonAdClient(authDO, region))
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

    public Long createAuth(String code) {
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
        // 临时的client
        var client = new AmazonAdClient(authDO, AmazonRegion.NA);
        refreshAccessTokenAndSave(authDO);
        var accountId = client.listAccounts().getAdsAccounts().get(0).getAdsAccountId();
        authDO.setAccountId(accountId);
        validate(authDO);
        refreshAccessTokenAndSave(authDO);
        return authDO.getId();
    }

    private void validate(AmazonAdAuthDO authDO) {
        var probe = new AmazonAdAuthDO();
        probe.setClientId(authDO.getClientId());
        probe.setAccountId(authDO.getAccountId());
        if (authRepository.findOne(Example.of(probe)).isPresent()) {
            throw new RuntimeException("Duplicate Authorization");
        }
    }



    @Scheduled(cron = "0 0,30 * * * *")
    public void refreshAuths() {
        auths.forEach(this::refreshAccessTokenAndSave);
    }

    private void refreshAccessTokenAndSave(AmazonAdAuthDO auth) {
        var newAccessToken = amazonService.refreshAccessToken(
            auth.getClientId(),
            clientRepository.findById(auth.getClientId()).get().getSecret(),
            auth.getRefreshToken()
        );
        auth.setAccessToken(newAccessToken);
        authRepository.save(auth);
    }

}
