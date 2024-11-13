package com.somle.walmart.service;


import com.somle.walmart.repository.WalmartTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// https://developer.walmart.com/api/us/mp/auth
@Slf4j
@Service
public class WalmartService {

    @Autowired
    private WalmartTokenRepository tokenRepository;

    @Getter
    private WalmartClient client;

    @PostConstruct
    public void init() {
        var token = tokenRepository.findAll().get(0);
        client = new WalmartClient(token);
    }


}