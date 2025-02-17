package com.somle.kingdee.service;


import com.somle.kingdee.model.KingdeeAuxInfoDetail;
import com.somle.kingdee.model.KingdeeProductSaveReqVO;
import com.somle.kingdee.model.KingdeeToken;
import com.somle.kingdee.model.supplier.KingdeeSupplier;
import com.somle.kingdee.repository.KingdeeTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

// https://open.jdy.com/#/files/api/detail?index=2&categrayId=3cc8ee9a663e11eda5c84b5d383a2b93&id=adfe4a24712711eda0b307c6992ee459
@Slf4j
@Service
@Data
@ConfigurationProperties(prefix = "kingdee")
public class KingdeeService {

    private List<String> outerInstanceIds;

    @Autowired
    private KingdeeTokenRepository tokenRepository;

    private List<KingdeeClient> clients;

    @PostConstruct
    public void init() {
        // clientList = tokenRepository.findAll().stream().map(n->new KingdeeClient(n)).toList();
        clients = outerInstanceIds.stream()
            .map(n -> new KingdeeClient(tokenRepository.findByOuterInstanceId(n)))
            .toList();
    }

    public KingdeeClient getClientByName(String name) {
        return new KingdeeClient(tokenRepository.findByAccountName(name));
    }


    @Scheduled(cron = "0 0 * * * *")
    public boolean refreshAuths() {
        return clients.parallelStream()
            .map(KingdeeClient::refreshAuth)
            .allMatch(this::saveToken);
    }

    public boolean saveToken(KingdeeToken token) {
        boolean success = false;
        try {
            tokenRepository.save(token);
            log.info("tokens saved successfully");
            success = true;
        } catch (DataAccessException e) {
            // Handle exception as needed
        }
        return success;
    }


    public void addDepartment(KingdeeAuxInfoDetail department) {
        clients.parallelStream().forEach(n-> n.addDepartment(department));
    }


    public void addProduct(KingdeeProductSaveReqVO product) {
        clients.parallelStream().forEach(n-> n.addProduct(product));
    }

    public void addSupplier(KingdeeSupplier kingdeeSupplier) {
        clients.parallelStream().forEach(n-> n.addSupplier(kingdeeSupplier));
    }

    /**
     *  获得数据库所有令牌
     * @return List<KingdeeToken>
     */
    public List<KingdeeToken> listKingdeeTokens () {
        return tokenRepository.findAll();
    }
}
