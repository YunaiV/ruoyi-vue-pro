package com.somle.manomano.service;


import com.somle.manomano.repository.ManomanoShopRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// https://www.manomano.dev/#intro
@Slf4j
@Service
public class ManomanoService {

    @Autowired
    private ManomanoShopRepository shopRepository;

    @Getter
    private ManomanoClient client;

    @PostConstruct
    public void init() {
        var shop = shopRepository.findAll().get(0);
        client = new ManomanoClient(shop);
    }


}