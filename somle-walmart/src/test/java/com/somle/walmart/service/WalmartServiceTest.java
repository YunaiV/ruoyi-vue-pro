package com.somle.walmart.service;


import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.walmart.model.reps.WalmartItemDetailResp;
import com.somle.walmart.model.req.WalmartAllProductsReqVO;
import com.somle.walmart.model.req.WalmartOrderReqVO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

@Disabled
@Slf4j
@Import({WalmartService.class})
class WalmartServiceTest extends SomleBaseDbUnitTest {
    @Resource
    WalmartService service;


    @Test
    void getAccessToken() {
        log.info(service.walmartClients.get(0).getAccessToken());
    }

    @Test
    void getOrders() {
        var start = LocalDateTime.now().minusDays(30);
        var end = LocalDateTime.now();

        var vo = WalmartOrderReqVO.builder()
            .limit(1)
            .createdStartDate(start)
            .createdEndDate(end)
            .build();
        log.info(service.walmartClients.get(0).getOrders(vo).toString());
    }


    @SneakyThrows
    @Test
    void getAllProducts() {
        var vo = WalmartAllProductsReqVO.builder()
            .offset(3075)
            .limit(200)
            .build();

//        service.walmartClients.get(0).retrieveSingleItemFullDetail("TT207001MBD");
        List<WalmartItemDetailResp> allProductDetails = service.walmartClients.get(0).getAllProductDetails(vo);
        return;
    }

//    @Test
//    void getAvailableReconFiles() {
//        log.info(service.getClient().getAvailableReconFileDates().toString());
//    }
//
//    @Test
//    void getReconFile() {
//        log.info(service.getClient().getReconFile("02272024"));
//    }
//
//    @Test
//    void getPaymentStatement() {
//        log.info(service.getClient().getPaymentStatement().toString());
//    }

    @Test
    void getInventory() {
        service.walmartClients.get(0).retrieveSingleItemFullDetail("DO304501WG");
//        service.walmartClients.get(0).retrieveSingleItemFullDetail("565274402");
    }
}