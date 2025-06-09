package com.somle.kingdee.service;


import com.somle.kingdee.model.*;
import com.somle.kingdee.model.supplier.KingdeeSupplierSaveVO;
import com.somle.kingdee.model.vo.KingdeeSupplierQueryReqVO;
import com.somle.kingdee.repository.KingdeeTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

// https://open.jdy.com/#/files/api/detail?index=2&categrayId=3cc8ee9a663e11eda5c84b5d383a2b93&id=adfe4a24712711eda0b307c6992ee459
@Slf4j
@Service
@Data
@ConfigurationProperties(prefix = "kingdee")
public class KingdeeService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    private KingdeeTokenRepository tokenRepository;
    @Autowired
    private RedissonClient redissonClient;

    private List<String> outerInstanceIds;
    private List<KingdeeClient> clients;

    //TODO 优化: 启动后异步初始化，懒加载
    @PostConstruct
    public void init() {
        // clientList = tokenRepository.findAll().stream().map(n->new KingdeeClient(n)).toList();
        this.clients = this.outerInstanceIds.stream()
            .map(n -> new KingdeeClient(tokenRepository.findByOuterInstanceId(n), stringRedisTemplate, redissonClient))
            .toList();
        log.debug("kingdee client size: {}", clients.size());
    }

    public KingdeeClient getClientByName(String name) {
        return new KingdeeClient(tokenRepository.findByAccountName(name), stringRedisTemplate, redissonClient);
    }


    @Scheduled(cron = "0 0 * * * *")
    public boolean refreshAuths() {
        this.init();
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

    public void addSupplier(KingdeeSupplierSaveVO kingdeeSupplierSaveVO) {
        clients.parallelStream().forEach(n -> n.addSupplier(kingdeeSupplierSaveVO));
    }


    /**
     * 保存采购订单
     *
     * @param purchaseOrder 采购订单
     */
    public void savePurchaseOrder(@Validated KingdeePurOrderSaveReqVO purchaseOrder) {
        clients.parallelStream().forEach(n -> n.savePurOrder(purchaseOrder));
    }

    /**
     * 保存采购入库单
     *
     * @param purInbound 采购入库单
     */
    public void savePurInbound(@Validated KingdeePurInboundSaveReqVO purInbound) {
        clients.parallelStream().forEach(n -> n.savePurInbound(purInbound));
    }

    /**
     * 保存采购出库单
     *
     * @param purOutbound 采购出库单
     */
    public void savePurOutbound(@Validated KingdeePurReturnSaveReqVO purOutbound) {
        clients.parallelStream().forEach(n -> n.savePurReturn(purOutbound));
    }

    /**
     *  获得数据库所有令牌
     * @return List<KingdeeToken>
     */
    public List<KingdeeToken> listKingdeeTokens () {
        return tokenRepository.findAll();
    }

    /**
     * 获取第一个公司的供应商集合
     * key:公司name
     */
    public Map<String, KingdeeSupplierSaveVO> getAllSupplierList(KingdeeSupplierQueryReqVO queryReqVO) {
        AtomicReference<Map<String, KingdeeSupplierSaveVO>> map = new AtomicReference<>();
        clients.stream().findFirst().ifPresent(peek -> map.set(peek.getAllSupplierList(queryReqVO)));
        return map.get();
    }

    /**
     * 删除供应商缓存
     *
     * @return 删除的缓存总数
     */
    public Integer deleteSupplierCache() {
        return clients.parallelStream()
            .mapToInt(KingdeeClient::deleteSupplierCache)
            .sum();
    }
}
