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
        entity.ifPresentOrElse(
                //优化方向？->每次实例化时判断是否token过期，自动刷新
                //2、维护本地map，多个client,volatile
                e -> {
                    if (this.client == null) {
                        this.client = new RakutenClient(e);
                        log.debug("RakutenClient initialized successfully.");
                    }
                    // 这里可以进一步扩展，例如检查 token 是否过期，如果过期可以刷新
                },
                () -> {
                    log.warn("RakutenTokenEntity Table is Empty");
                    throw new RuntimeException("RakutenTokenEntity  is Empty");
                }
        );
    }

}

