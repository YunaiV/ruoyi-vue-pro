package com.somle.home24.service;

import com.somle.home24.repository.Home24AccountRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Home24Service {
    public List<Home24Client> clients;
    @Resource
    private Home24AccountRepository home24AccountRepository;

    @PostConstruct
    private void init() {
        clients = home24AccountRepository.findAll().stream()
            .map(Home24Client::new)
            .toList();
    }


}
