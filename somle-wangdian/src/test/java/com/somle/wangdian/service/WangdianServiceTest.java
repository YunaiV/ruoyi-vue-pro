package com.somle.wangdian.service;

import com.somle.framework.test.core.ut.BaseDbUnitTest;
import com.somle.framework.test.core.ut.BaseSpringTest;
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
        Map<String, String> params = new HashMap<String, String>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        params.put("start_time", LocalDateTime.of(2024,11,7,8,0).format(formatter));
        params.put("end_time", LocalDateTime.of(2024,11,7,9,0).format(formatter));
        params.put("page_size", "30");
        params.put("page_no", "0");

        var result = service.client.execute("trade_query.php", params);

        log.info(result.toString());
    }



}