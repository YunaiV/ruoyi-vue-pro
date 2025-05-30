package com.somle.manomano.service;


import com.somle.manomano.repository.ManomanoShopRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// https://www.manomano.dev/#intro
@Slf4j
@Service
public class ManomanoService {

    @Autowired
    private ManomanoShopRepository shopRepository;

    public List<ManomanoClient> clients;

    @PostConstruct
    public void init() {
        clients = shopRepository.findAll().stream().map(ManomanoClient::new).toList();
    }


}