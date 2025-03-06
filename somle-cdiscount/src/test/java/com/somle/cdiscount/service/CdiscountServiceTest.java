package com.somle.cdiscount.service;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({CdiscountService.class})
class CdiscountServiceTest extends BaseDbUnitTest {
    @Resource
    CdiscountService service;

    @Test
    void test(){
        log.info(service.client.getOrders().toString());
    }
}