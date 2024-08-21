package com.somle.eccang;

import com.somle.eccang.repository.EccangTokenRepository;
import com.somle.eccang.service.EccangService;
import com.somle.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;


@Slf4j
@Import({
    EccangService.class,
})
public class EccangTest extends BaseSpringTest {

    @Resource
    EccangService eccangService;

    @Resource
    EccangTokenRepository eccangTokenRepository;

    @MockBean(name="dataChannel")
    MessageChannel dataChannel;

    @MockBean(name="saleChannel")
    MessageChannel saleChannel;


    @Test
    void list() {
        var result = eccangService.list("getWarehouse");
        System.out.println(result.toString());
    }

    @Test
    void post() {
    }

    @Test
    public void testRepo() {
        Assertions.assertEquals(eccangTokenRepository.findAll().size(), 2);
        System.out.println("correct count");
    }
}