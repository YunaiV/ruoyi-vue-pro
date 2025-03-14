package com.somle.otto.service;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.otto.model.resp.OttoCommonResp;
import com.somle.otto.repository.OttoAccountDao;
import com.somle.otto.repository.OttoAuthTokenDao;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

@Disabled
@Slf4j
@Import({OttoService.class, OttoAccountDao.class, OttoAuthTokenDao.class})
class OttoClientTest extends SomleBaseDbUnitTest {

    @Resource
    OttoAccountDao ottoAccountDao;
    @Resource
    OttoAuthTokenDao ottoAuthTokenDao;
    @Resource
    OttoService service;


    @Test
    @Transactional
    @Rollback(value = false)
    void refreshToken() {
        OttoClient client = new OttoClient(ottoAccountDao.findAll().get(0));

        service.refreshToken(client);
        ottoAccountDao.findAll().forEach(System.out::println);
    }


    @Test
    void getSettlement() {
        refreshToken();
        //
        OttoClient client = new OttoClient(ottoAccountDao.findAll().get(0));
        OttoCommonResp<Object> resp = client.getSettlement("2024-12-01", "2024-12-02");
        for (Object resource : resp.getResources()) {
            log.info("resource = {}", resource);
        }
    }


    @Test
    void getOrders() {
        refreshToken();
        //
        OttoClient client = new OttoClient(ottoAccountDao.findAll().get(0));
        OttoCommonResp<Object> orders = client.getOrders("2024-12-01", "2024-12-02");
        for (Object resource : orders.getResources()) {
            System.out.println("resource = " + resource);
        }
        System.out.println("orders.resources.size() = " + orders.resources.size());

    }


}