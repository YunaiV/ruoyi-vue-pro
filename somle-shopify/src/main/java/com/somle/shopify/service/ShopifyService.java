package com.somle.shopify.service;

import com.somle.shopify.model.ShopifyToken;
import com.somle.shopify.repository.ShopifyTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class ShopifyService {
    @Autowired
    ShopifyTokenRepository tokenRepository;

    public ShopifyClient client;

    @PostConstruct
    public void init() {
        client = new ShopifyClient(tokenRepository.findAll().get(0));
    }


    public List<ShopifyClient> getAllShopifyClients() {
        List<ShopifyClient> clients = tokenRepository.findAll().stream().map(ShopifyClient::new).toList();
        return clients;
    }
}