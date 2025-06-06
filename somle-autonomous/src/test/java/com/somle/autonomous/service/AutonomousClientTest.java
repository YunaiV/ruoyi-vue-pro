package com.somle.autonomous.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.autonomous.enums.AutonomousOrderStatus;
import com.somle.autonomous.model.AutonomousAccount;
import com.somle.autonomous.repository.AutonomousAccountRepository;
import com.somle.autonomous.req.AutonomousOrderReq;
import com.somle.autonomous.resp.AutonomousOrderResp;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

@Slf4j
@DataJpaTest
@Disabled
class AutonomousClientTest extends SomleBaseDbUnitTest {

    @Resource
    AutonomousAccountRepository autonomousAccountRepository;


    @Test
    @Rollback(value = false)
    @Transactional
    void getAutonomousAccount() {
        AutonomousAccount account = new AutonomousAccount();
        account.setEmail("cs06@fitueyes.com");
        account.setPassword("new@Somle1223");
        account.setGrantType("password");
        autonomousAccountRepository.save(account);
        Optional<AutonomousAccount> first = autonomousAccountRepository.findAll()
            .stream()
            .findFirst();
        System.out.println("first = " + first);
    }


    @Test
    void testGetOrder() {
        //拿第一个账户  获取范围内时间的所有订单
        autonomousAccountRepository.findAll()
            .stream()
            .findFirst()
            .ifPresent(
                autonomousAccount -> {
                    AutonomousClient client = new AutonomousClient(autonomousAccount);

                    AutonomousOrderReq req = AutonomousOrderReq.builder()
                        .dateFrom("2024-01-05T01:20:43.540Z")
                        .dateTo("2024-12-25T01:20:43.540Z")
                        .limit(100)
                        .build();
                    AutonomousOrderResp order = client.getOrder(req);
                    System.out.println("order = " + order);

//                    client.getOrder()
                }
            );
    }

    @Test
    void getSettlementOrder() {
        //获取状态为已发货的订单，结算需要手动汇总，且不统计部分退款
        autonomousAccountRepository.findAll()
            .stream()
            .findFirst()
            .ifPresent(
                autonomousAccount -> {
                    AutonomousClient client = new AutonomousClient(autonomousAccount);

                    AutonomousOrderReq req = AutonomousOrderReq.builder()
                        .dateFrom("2024-12-01T01:20:43.540Z")
                        .dateTo("2024-12-25T01:20:43.540Z")
                        .statuses(AutonomousOrderStatus.DELIVERED.getValue())
                        .limit(100)
                        .build();
                    AutonomousOrderResp order = client.getSettlementOrder(req);
                    System.out.println("getSettlementOrder = " + order);

//                    client.getOrder()
                }
            );
    }
}