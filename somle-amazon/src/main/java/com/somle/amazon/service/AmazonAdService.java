package com.somle.amazon.service;

import com.somle.amazon.controller.vo.AmazonAuthReqVO;
import com.somle.amazon.controller.vo.AmazonAuthRespVO;
import com.somle.amazon.model.AmazonAccount;
import com.somle.amazon.model.AmazonAdAuthDO;
import com.somle.amazon.model.AmazonSeller;
import com.somle.amazon.repository.AmazonAccountRepository;
import com.somle.amazon.repository.AmazonAdAuthRepository;
import com.somle.amazon.repository.AmazonAdClientRepository;
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
public class AmazonAdService {
    @Resource
    AmazonService amazonService;

    @Resource
    AmazonAdAuthRepository authRepository;

    @Resource
    AmazonAdClientRepository clientRepository;

    public List<AmazonAdClient> clients;

    @PostConstruct
    public void init() {
        clients = authRepository.findAll().stream()
            .map(AmazonAdClient::new)
            .toList();
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
