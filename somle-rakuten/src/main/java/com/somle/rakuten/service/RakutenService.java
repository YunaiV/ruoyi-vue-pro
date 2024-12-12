package com.somle.rakuten.service;

import com.somle.rakuten.model.pojo.RakutenTokenEntityDO;
import com.somle.rakuten.repository.RakutenTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RakutenService {
    private final RakutenTokenRepository repository;
    // 提供客户端访问接口
    @Getter
    public RakutenClient client;

    @PostConstruct
    public void init() {
        //暂时拿第一个
        Optional<RakutenTokenEntityDO> entity = repository.findAll().stream().findFirst();
        entity.ifPresent(e -> this.client = new RakutenClient(e));
        if (entity.isPresent()) {
            this.client = new RakutenClient(entity.get());
        } else {
            log.warn("RakutenTokenEntity Table is Empty");
            throw new RuntimeException("RakutenTokenEntity  is Empty");
        }
    }

}
