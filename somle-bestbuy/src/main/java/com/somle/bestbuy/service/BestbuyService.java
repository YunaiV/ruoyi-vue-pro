package com.somle.bestbuy.service;

import com.somle.bestbuy.repository.BestbuyTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务类，用于与Bestbuy API进行交互。
 *
 * @author: gumaomao
 * @date: 2024/12/13 13:49
 */
@Slf4j
@Service
public class BestbuyService {
    @Resource
    private BestbuyTokenRepository bestbuyTokenRepository;

    public List<BestbuyClient> bestbuyClients;

    @PostConstruct
    public void init() {
        this.bestbuyClients = bestbuyTokenRepository.findAll().stream()
            .map(BestbuyClient::new)
            .toList();
    }
}
