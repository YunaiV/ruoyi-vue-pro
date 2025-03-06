package com.somle.cdiscount.service;
import com.somle.cdiscount.repository.CdiscountTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CdiscountService {

    public CdiscountClient client;


    @Resource
    public CdiscountTokenRepository tokenRepository;

    @PostConstruct
    public void init() {
        client = new CdiscountClient(tokenRepository.findAll().get(0));
    }
}
