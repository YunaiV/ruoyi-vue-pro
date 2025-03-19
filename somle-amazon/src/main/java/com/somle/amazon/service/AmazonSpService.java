package com.somle.amazon.service;

import com.somle.amazon.repository.AmazonSpAuthRepository;
import com.somle.amazon.repository.AmazonSpClientRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AmazonSpService {
    @Resource
    AmazonService amazonService;

    @Resource
    AmazonSpAuthRepository authRepository;

    @Resource
    AmazonSpClientRepository clientRepository;

    public List<AmazonSpClient> clients;

    @PostConstruct
    public void init() {
        clients = authRepository.findAll().stream()
            .map(AmazonSpClient::new)
            .toList();
    }


    @Scheduled(cron = "0 0,30 * * * *")
    public void refreshAuths() {
        clients.forEach(this::refreshAuth);
    }

    private void refreshAuth(AmazonSpClient client) {
        var auth = client.getAuth();
        var newAccessToken = amazonService.refreshAccessToken(
            auth.getClientId(),
            clientRepository.findById(auth.getClientId()).get().getSecret(),
            auth.getRefreshToken()
        );
        auth.setAccessToken(newAccessToken);
        client.setAuth(auth);
        authRepository.save(auth);
    }

}
