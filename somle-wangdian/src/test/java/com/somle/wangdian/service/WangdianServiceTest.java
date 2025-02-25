package com.somle.wangdian.service;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.BaseSpringTest;
import com.somle.wangdian.model.WangdianTradeReqVO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Import(WangdianService.class)
class WangdianServiceTest extends BaseDbUnitTest {

    @Resource
    WangdianService service;

    @Test
    @SneakyThrows
    public void test() {
        var startTime = LocalDateTime.of(2024,11,7,8,0);
        var endTime = LocalDateTime.of(2024,11,7,9,0);


        var reqVO = WangdianTradeReqVO.builder()
            .startTime(startTime)
            .endTime(endTime)
            .pageSize(100)
            .pageNo(0)
            .build();

        var result = service.client.execute("trade_query.php", reqVO);

        log.info(result.toString());
    }



}