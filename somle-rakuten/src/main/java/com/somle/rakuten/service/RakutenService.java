package com.somle.rakuten.service;

import com.somle.rakuten.repository.RakutenTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RakutenService {
    private final RakutenTokenRepository repository;
    // 提供客户端访问接口
    @Getter
    public List<RakutenClient> rakutenClients;

    @PostConstruct
    public void init() {
        this.rakutenClients = repository.findAll().stream().map(RakutenClient::new).toList();
    }

}

