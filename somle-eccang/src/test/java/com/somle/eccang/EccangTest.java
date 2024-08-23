package com.somle.eccang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.somle.eccang.model.EccangOrderVO;
import com.somle.eccang.repository.EccangTokenRepository;
import com.somle.eccang.service.EccangService;
import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Slf4j
@Import({
    EccangService.class,
})
public class EccangTest extends BaseSpringTest {
    @Resource
    EccangService service;

    @Resource
    EccangTokenRepository tokenRepo;


    @MockBean(name="dataChannel")
    MessageChannel dataChannel;

    @MockBean(name="saleChannel")
    MessageChannel saleChannel;

    @BeforeEach
    void init() {
        log.info("run init");
    }


    @Test
    void list() {
        var result = service.list("getWarehouse");
        System.out.println(result.toString());
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
    public void testRepo() {
        Assertions.assertEquals(2, tokenRepo.findAll().size());
    }

    @Test
    public void getOrder() {
        var date = LocalDate.of(2024,8,6);
        var time1 = LocalTime.of(0,0,0);
        var time2 = LocalTime.of(23,59,59);
        var result = service.getOrderPages(
            EccangOrderVO.builder()
                .warehouseShipDateStart(LocalDateTime.of(date,time1))
                .warehouseShipDateEnd(LocalDateTime.of(date,time2))
                .platform("walmart")
            .build()
        );
        log.info(result.toList().toString());
    }
}