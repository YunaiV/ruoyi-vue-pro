package com.somle.walmart.service;


import com.somle.walmart.repository.WalmartTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class WalmartService {

    @Autowired
    private WalmartTokenRepository tokenRepository;


    private WalmartClient client;

    public WalmartClient getClient() {
        if (client == null) {
            var token = tokenRepository.findAll().get(0);
            if (token.getSvcName().equals("Walmart Marketplace")) {
                client = new WalmartMarketplaceClient(token);
            } else {
                client = new WalmartDsvClient(token, "752076");
            }
        }
        return client;
    }

}