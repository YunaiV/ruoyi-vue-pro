package com.somle.cdiscount.service;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CdiscountService {

    public CdiscountClient client;

    @PostConstruct
    public void init() {
        client = new CdiscountClient();
    }
}
