package com.somle.rakuten.service;

import com.somle.rakuten.model.pojo.RakutenTokenEntity;
import com.somle.rakuten.repository.RakutenTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RakutenService {
    private final RakutenTokenRepository repository;
    public RakutenClient client;

    @PostConstruct
    public void init() {
        //暂时拿第一个
        Optional<RakutenTokenEntity> entity = repository.findAll().stream().findFirst();
        entity.ifPresent(e -> this.client = new RakutenClient(e));
        if (entity.isEmpty()) {
            log.warn("RakutenTokenEntity Table is Empty");
            throw new RuntimeException("RakutenTokenEntity  is Empty");
        }
    }

}
