package com.somle.shopee.service;


import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.shopee.model.ShopeeAccount;
import com.somle.shopee.model.reps.ShopeeOrderDetailReps;
import com.somle.shopee.model.reps.ShopeeOrderListReps;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import java.time.*;
import java.util.List;

@Disabled
@Slf4j
@Import({ShopeeService.class})
class ShopeeServiceTest extends SomleBaseDbUnitTest {
    @Resource
    ShopeeService service;

    @Test
    @SneakyThrows
    public void test() {
        ShopeeAccount account = service.clients.get(0).getAccount();
        service.clients.get(0).getRefreshTokenAndAccessToken("414a4d74514a55584766476a46535154", account.getPartnerId(), account.getPartnerKey(), account.getShopId());
    }

    @Test
    @SneakyThrows
    @Rollback(false)
    public void refreshToken() {
        System.out.println(111);
        ShopeeAccount account = service.clients.get(0).getAccount();
        service.refreshAuths();
    }

    @Test
    @SneakyThrows
    void getProducts() {
        System.out.println(111);
        ShopeeAccount account = service.clients.get(0).getAccount();

//        String accessToken = service.clients.get(0).getAccessTokenByRefreshToken(account.getRefreshToken(), account.getPartnerId(), account.getPartnerKey(), account.getShopId());

        service.clients.get(0).getAllProducts();
    }

    @Test
    @SneakyThrows
    void getOrders() {
        System.out.println(111);
        ShopeeAccount account = service.clients.get(0).getAccount();

//        String accessToken = service.clients.get(0).getAccessTokenByRefreshToken(account.getRefreshToken(), account.getPartnerId(), account.getPartnerKey(), account.getShopId());


        // 日期字符串
        String dateStr = "2025-05-01";
        String dateStr2 = "2025-05-11";

        // 解析为LocalDate（默认ISO格式，无需指定格式化器）
        LocalDate date = LocalDate.parse(dateStr);
        LocalDate date2 = LocalDate.parse(dateStr2);

        // 转换为某时区当天的零点时间（例如：系统默认时区）
        ZonedDateTime zonedDateTime = date.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime zonedDateTime2 = date2.atStartOfDay(ZoneId.systemDefault());

        // 获取时间戳（毫秒）
        long start = zonedDateTime.toInstant().toEpochMilli();
        long end = zonedDateTime2.toInstant().toEpochMilli();

        // 获取当前秒级时间戳
        long endTime = System.currentTimeMillis() / 1000L;

// 计算7天前的秒级时间戳（固定86400秒/天）
        long startTime = endTime - 7 * 24 * 60 * 60;

        ShopeeOrderListReps orderList = service.clients.get(0).getOrderList(startTime, endTime, "");
        StringBuilder sb = new StringBuilder();
        for (ShopeeOrderListReps.Response.Order order : orderList.getResponse().getOrderList()) {
            sb.append(order.getOrderSn()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        service.clients.get(0).getAllOrders(startTime, endTime);


    }


    @Test
    public void listOrders() {
        String param = "2025-05-25";
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59);


        // 将 LocalDateTime 转换为指定时区的 ZonedDateTime
        ZoneId zoneId = ZoneId.of("Asia/Shanghai"); // 可以根据需求更换时区
        ZonedDateTime zonedDateTime = startTime.atZone(zoneId);
        ZonedDateTime zonedDateTime2 = endTime.atZone(zoneId);

        // 转换为 Instant
        Instant instant = zonedDateTime.toInstant();
        Instant instant2 = zonedDateTime2.toInstant();

        // 获取秒级时间戳
        long startTimestamp = instant.getEpochSecond();
        long endTimestamp = instant2.getEpochSecond();


        List<ShopeeOrderDetailReps.Order> allOrders = service.clients.get(0).getAllOrders(startTimestamp, endTimestamp);
        System.out.println(allOrders);
    }


}