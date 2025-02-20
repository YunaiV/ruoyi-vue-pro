package com.somle.amazon.service;

import cn.iocoder.yudao.framework.common.util.general.CoreUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseSpringTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;


@Slf4j
@Import({
    AmazonService.class,
    AmazonAdService.class,
})
class AmazonAdServiceTest extends BaseSpringTest {
    @Resource
    AmazonAdService service;

    @Resource
    AmazonService amazonService;


    @Test
    void refreshAuths() {
        service.refreshAuths();
    }

    @Test
    void generateAccessToken() {
        var clientDO = service.clientRepository.findAll().get(0);
        var response = amazonService.generateAccessToken(
            clientDO.getId(),
            clientDO.getSecret(),
            "ANWfdLbCJdnfYyzVsKXa",
            service.REDIRECT_URI
        );
        log.info(response.toString());
    }


}