package com.somle.tiktok.service;

import com.somle.tiktok.model.TikTokAccount;
import com.somle.tiktok.model.resp.TikTokAuthResp;
import com.somle.tiktok.repository.TikTokAccountRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TikTokService {
    public List<TikTokClient> tikTokClients;
    @Resource
    TikTokAccountRepository tikTokAccountRepository;

    @PostConstruct
    public void init() {
        List<TikTokClient> all = new ArrayList<>();
        for (TikTokAccount tikTokAccount : tikTokAccountRepository.findAll()) {
            all.add(new TikTokClient(tikTokAccount));
        }
        tikTokClients = all;
    }

    @Scheduled(cron = "0 0,0 1 * * *")
    public void refreshAccessToken() {
        tikTokClients.stream().forEach(client -> {
            TikTokAccount tikTokAccount = client.getTikTokAccount();
            TikTokAuthResp tikTokAuthResp = client.getAccessToken(tikTokAccount);
            tikTokAccount.setAccessToken(tikTokAuthResp.getData().getAccessToken());
            client.setTikTokAccount(tikTokAccount);
            tikTokAccountRepository.save(tikTokAccount);
        });
    }
}
