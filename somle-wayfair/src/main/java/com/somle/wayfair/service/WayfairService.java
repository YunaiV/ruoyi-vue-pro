package com.somle.wayfair.service;

import com.somle.wayfair.repository.WayfairTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class WayfairService {
    @Autowired
    WayfairTokenRepository tokenRepository;

    public List<WayfairClient> clients;

    @PostConstruct
    public void init() {
        clients = tokenRepository.findAll().stream().map(n -> new WayfairClient(n)).toList();
    }
}
