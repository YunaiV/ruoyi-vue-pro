package com.wangdian.service;

import com.wangdian.api.WdtClient;
import com.wangdian.repository.WangdianTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WangdianService {
    @Autowired
    WangdianTokenRepository tokenRepository;

    public WdtClient client;

    @PostConstruct
    public void init() {
        var token = tokenRepository.findAll().get(0);
        var baseUrl = "http://api.wangdian.cn/openapi2/";
        client = new WdtClient(token.getSid(),token.getAppkey(),token.getAppsecret(),baseUrl);
    }
}
