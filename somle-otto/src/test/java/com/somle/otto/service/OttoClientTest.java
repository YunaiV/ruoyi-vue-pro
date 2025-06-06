package com.somle.otto.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.otto.repository.OttoAccountDao;
import com.somle.otto.repository.OttoAuthTokenDao;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        service.ottoClients.stream().forEach(client -> {
            client.refreshToken(client);
        });
        ottoAccountDao.findAll().forEach(System.out::println);
    }


//    @Test
//    void getSettlement() {
//        refreshToken();
//        //
//        OttoClient client = new OttoClient(ottoAccountDao.findAll().get(0));
//        OttoCommonResp<Object> resp = client.getSettlement("2024-12-01", "2024-12-02");
//        for (Object resource : resp.getResources()) {
//            log.info("resource = {}", resource);
//        }
//    }


    @Test
    void getOrders() {
        String param = "2025-05-01";
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59).plusDays(10);



        refreshToken();
        //
        OttoClient client = service.ottoClients.get(0);
        client.getAllOrders(startTime.toString(), endTime.toString());
    }

    @Test
    void getProduct() {
        //
        OttoClient client = service.ottoClients.get(0);
        client.getAllProducts();
    }

    @Test
    void getSkuQuantity() {
        //
        OttoClient client = service.ottoClients.get(0);
        client.getSkuQuantity("F02M1442B");
    }


}