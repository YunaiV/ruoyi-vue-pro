package com.somle.wayfair.service;

import com.somle.wayfair.repository.WayfairTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WayfairService {
    @Autowired
    WayfairTokenRepository tokenRepository;

    public WayfairClient client;

    @PostConstruct
    public void init() {
        client = new WayfairClient(tokenRepository.findAll().get(0));
    }
}
