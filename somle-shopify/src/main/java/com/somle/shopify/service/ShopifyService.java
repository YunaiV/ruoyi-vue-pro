package com.somle.shopify.service;

import com.somle.shopify.repository.ShopifyTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShopifyService {
    @Autowired
    ShopifyTokenRepository tokenRepository;

    public ShopifyClient client;

    @PostConstruct
    public void init() {
        log.info(tokenRepository.findAll().get(0).getAccessToken());
        client = new ShopifyClient(tokenRepository.findAll().get(0));
    }
}
