package com.somle.lazada.service;

import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.lazada.sdk.api.LazopClient;
import com.somle.lazada.sdk.api.LazopRequest;
import com.somle.lazada.sdk.api.LazopResponse;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


@Disabled
@Slf4j
@Import({
    LazadaService.class
})
class LazadaServiceTest extends SomleBaseDbUnitTest {
    //    @Resource
//    ShopifyClient client;
    @Resource
    LazadaService service;

    @Test
    @SneakyThrows
    void test() {
        LazopClient client = new LazopClient("https://api.lazada.com/rest", "133309", "NMsCUm6Lqsp5j8xixIXIUTuZvof58Zbz");
        LazopRequest request = new LazopRequest("/auth/token/create");
        request.addApiParameter("code", "0_133309_bPFp7qzu3hHDE3rEgRYi2pzl688");
        LazopResponse response = client.execute(request);
        String body = response.getBody();
        System.out.println(response.getBody());
    }

    @Test
    @SneakyThrows
    //测试类默认会回滚，所以不会往数据库插入数据，加@Rollback(false)可以避免
    @Rollback(false)
    void test1() {
        service.refreshAuths();
    }

//   店铺授权链接 https://auth.lazada.com/oauth/authorize?response_type=code&force_auth=true&redirect_uri=https://erp.somle.com/&client_id=133309

    @Test
    @SneakyThrows
    void test2() {
        service.clients.get(0).getAllProducts();
    }

    @Test
    @SneakyThrows
    void test3() {
        LocalDate baseDate = LocalDate.parse("2025-05-14");
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(20);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
//        LocalDateTime endTime = targetDate.atTime(23, 59, 59);

        LocalDateTime endTime = LocalDateTime.now();
        service.clients.get(0).getAllOrders(startTime, startTime);
    }

    @Test
    public void test4() {
        String dateStr = "2025-04-25 15:15:36 +0800";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xx");
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateStr, formatter);
        LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
        System.out.println(localDateTime); // 输出：2025-04-25T15:15:36
    }

}