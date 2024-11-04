package com.somle.kingdee.service;


import com.somle.kingdee.model.KingdeeAuxInfoDetail;
import com.somle.kingdee.model.KingdeeDepartmentDetail;
import com.somle.kingdee.model.KingdeeProduct;
import com.somle.kingdee.model.KingdeeToken;
import com.somle.kingdee.repository.KingdeeTokenRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import jakarta.annotation.PostConstruct;

// https://open.jdy.com/#/files/api/detail?index=2&categrayId=3cc8ee9a663e11eda5c84b5d383a2b93&id=adfe4a24712711eda0b307c6992ee459
@Slf4j
@Service
@Data
@ConfigurationProperties(prefix = "kingdee")
public class KingdeeService {

    private List<String> outerInstanceIds;

    @Autowired
    private KingdeeTokenRepository tokenRepository;

    private List<KingdeeClient> clientList;

    @PostConstruct
    public void init() {
        // clientList = tokenRepository.findAll().stream().map(n->new KingdeeClient(n)).toList();
        clientList = outerInstanceIds.stream()
            .map(n -> new KingdeeClient(tokenRepository.findByOuterInstanceId(n)))
            .toList();
    }

    public KingdeeClient getClientByName(String name) {
        return new KingdeeClient(tokenRepository.findByAccountName(name));
    }


    @Scheduled(cron = "0 0 3 * * *") // Executes at 3:00 AM every day
    public boolean refreshAuths() {
        return clientList.parallelStream()
            .map(n->n.refreshAuth())
            .map(n->saveToken(n))
            .allMatch(n->n==true);
    }

    public boolean saveToken(KingdeeToken token) {
        boolean success = false;
        try {
            tokenRepository.save(token);
            log.info("tokens saved successfully");
            success = true;
        } catch (DataAccessException e) {
            System.out.println("Save failed: " + e.getMessage());
            // Handle exception as needed
        }
        return success;
    }


    public void addDepartment(KingdeeAuxInfoDetail department) {
        clientList.parallelStream().forEach(n->n.addDepartment(department));
    }


    public void addProduct(KingdeeProduct product) {
        clientList.parallelStream().forEach(n->n.addProduct(product));
    }

}
