package com.somle.eccang;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseSpringTest;
import com.somle.eccang.config.EccangIntegrationConfig;
import com.somle.eccang.model.*;
import com.somle.eccang.repository.EccangTokenRepository;
import com.somle.eccang.service.EccangService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Disabled
@Slf4j
@Import({
    EccangService.class,
    EccangIntegrationConfig.class,
})
public class EccangTest extends SomleBaseSpringTest {
    @Resource
    EccangService service;

    @Resource
    EccangTokenRepository tokenRepo;




    @BeforeEach
    void init() {
        log.info("run init");
    }


    @Test
    void list() {
        List<EccangShippingMethod> list = service.getShippingMethod().toList();
        log.info(list.toString());
    }

    @Test
    void post() {
    }

    @Test
    public void testInit() {
    }

    @Test
    void getWarehouseList() {
        var result = service.getWarehouseList();
        log.info(result.toString());
    }

    @Test
    void getInventory() {
        var result = service.getInventory();
        log.info(result.toString());
    }

    @Test
    void getInventoryBatchLog() {
        var date = LocalDate.of(2024, 12, 19);
        var time1 = LocalTime.of(0, 0, 0);
        var time2 = LocalTime.of(23, 59, 59);
        var datetime1 = LocalDateTime.of(date, time1);
        var datetime2 = LocalDateTime.of(date, time2);
        EccangInventoryBatchLogVO vo = new EccangInventoryBatchLogVO();
        vo.setDateFrom(datetime1);
        vo.setDateTo(datetime2);
        var result = service.getInventoryBatchLog(vo);
        log.error(String.valueOf(result.toList().size()));
    }


    @Test
    public void testRepo() {
        Assertions.assertEquals(2, tokenRepo.findAll().size());
    }

    @Test
    public void getOrder() {
        var date = LocalDate.of(2023, 12, 10);
        var time1 = LocalTime.of(0, 0, 0);
        var time2 = LocalTime.of(23, 59, 59);
        var datetime1 = LocalDateTime.of(date, time1);
        var datetime2 = LocalDateTime.of(date, time2);
        var result = service.getOrderArchivePages(
            EccangOrderVO.builder()
                .condition(EccangOrderVO.Condition.builder()
                    .platformPaidDateStart(datetime1)
                    .platformPaidDateEnd(datetime2)
                    .build())

                .build(),
            2023
        );
        log.info(String.valueOf(result.toList().size()));
    }

    @Test
    public void getOrder2() {
        var reqVO = EccangOrderVO.builder()
            .condition(
                EccangOrderVO.Condition.builder()
                    .platformCreateDateStart(LocalDateTime.of(2025, 1, 1,0,0))
                    .platformCreateDateEnd(LocalDateTime.of(2025, 1, 2,0,0))
                    .build()
            )
            .build();
        log.info(service.getOrderUnarchivePages(reqVO).toList().toString());
    }

    @Test
    void test1() {
        var date = LocalDate.of(2023, 12, 10);
        var time1 = LocalTime.of(0, 0, 0);
        var time2 = LocalTime.of(23, 59, 59);
        var datetime1 = LocalDateTime.of(date, time1);
        var datetime2 = LocalDateTime.of(date, time2);
        var result = service.getOrderUnarchivePages(
            EccangOrderVO.builder()
                .condition(EccangOrderVO.Condition.builder()
                    .platformPaidDateStart(datetime1)
                    .platformPaidDateEnd(datetime2)
                    .build())

                .build()
        );
        log.info(String.valueOf(result.toList().size()));
    }

    @Test
    void test2() {
        var date = LocalDate.of(2023, 12, 10);
        var time1 = LocalTime.of(0, 0, 0);
        var time2 = LocalTime.of(23, 59, 59);
        var datetime1 = LocalDateTime.of(date, time1);
        var datetime2 = LocalDateTime.of(date, time2);
        var result = service.getOrderArchivePages(
            EccangOrderVO.builder()
                .condition(EccangOrderVO.Condition.builder()
                    .platformPaidDateStart(datetime1)
                    .platformPaidDateEnd(datetime2)
                    .build())

                .build(),
            2023
        );
        log.info(String.valueOf(result.toList().size()));
    }

    @Test
    void getRmaRefundList() {
        EccangRmaRefundVO vo = new EccangRmaRefundVO();
        vo.setRefundDateForm(LocalDateTime.now().minusDays(5));
        vo.setRefundDateTo(LocalDateTime.now().minusDays(4));
        List<EccangResponse.EccangPage> list = service.getRmaRefundList(vo).toList();
        log.info(list.toString());
    }


}