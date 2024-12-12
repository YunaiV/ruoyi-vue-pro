package com.somle.rakuten.service;


import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.test.core.ut.BaseDbUnitTest;
import com.somle.rakuten.model.polo.RakutenTokenEntity;
import com.somle.rakuten.model.vo.OrderRequestVO;
import com.somle.rakuten.model.vo.OrderSearchRequestVO;
import com.somle.rakuten.repository.RakutenTokenRepository;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Import({RakutenService.class})
class RakutenServiceTest extends BaseDbUnitTest {

    @Resource
    RakutenService service;
    @Resource
    RakutenTokenRepository repository;

    @Test
    void getRakuten() {
        ArrayList<String> list = new ArrayList<>();
        list.add("372286-20241210-0870710097");

        OrderRequestVO vo = OrderRequestVO.builder()
                .version(8)
                .orderNumberList(list)
                .build();

        JSONObject jsonObject = service.client.getOrders(vo);
        System.out.println(jsonObject);
    }

    @Test
    void getRakutenToken() {
        List<RakutenTokenEntity> all = repository.findAll();
        System.out.println("all = " + all);
    }

    @Test
    @Rollback(false)
    @Transactional
    void generateOne() {
        List<RakutenTokenEntity> all = repository.findAll();
        if ((long) all.size() == 0) {
            RakutenTokenEntity entity = new RakutenTokenEntity();
            entity.setServiceSecret("SP372286_XUye81AijnBIAYVK");
            entity.setLicenseKey("SL372286_XhRtwKoTGQyurnBF");
            RakutenTokenEntity save = repository.save(entity);
            log.info("save = {}", save);
        }
        getRakutenToken();
    }


    @SneakyThrows
    @Test
    void urlTest() {
        String endpoint = "/es/2.0/order/getOrder/";
        String BASE_URL = "https://api.rms.rakuten.co.jp";

        // 构造请求头
        HashMap<String, String> mapHeader = new HashMap<>();
        mapHeader.put("Authorization", "ESA U1AzNzIyODZfWFV5ZTgxQWlqbkJJQVlWSzpTTDM3MjI4Nl9YaFJ0d0tvVEdReXVybkJG");
        mapHeader.put("Content-Type", "application/json; charset=utf-8");

        // 构造请求体（OrderRequestVO）
        OrderRequestVO vo = OrderRequestVO.builder()
                .version(8)
                .orderNumberList(List.of("372286-20241210-0870710097"))
                .build();

        // 发送 POST 请求
        HttpResponse response = HttpRequest.post(BASE_URL + endpoint)
                .headerMap(mapHeader, true) // 设置头信息
                .body(JSONUtil.toJsonStr(vo)) // 设置请求体
                .timeout(2 * 1000) // 设置超时时间
                .execute();

        // 打印响应结果
//        JSONObject jsonNodes = JsonUtils.parseObject(response.body(), JSONObject.class);
        System.out.println("response.body() = " + JSONUtil.toBean(response.body(), JSONObject.class));
    }

    @Test
    void searchOrder() {
        // 使用日本时区 (Asia/Tokyo) 来解析 startDatetime 和 endDatetime
        ZonedDateTime startDatetime = LocalDateTimeUtil.parse("2024-10-01 00:00:00", "yyyy-MM-dd HH:mm:ss")
                .atZone(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime endDatetime = LocalDateTimeUtil.parse("2024-12-01 00:00:00", "yyyy-MM-dd HH:mm:ss")
                .atZone(ZoneId.of("Asia/Tokyo"));

        OrderSearchRequestVO vo = OrderSearchRequestVO.builder()
                .dateType(3)
                .startDatetime(startDatetime)
                .endDatetime(endDatetime)
                .build();
        JSONObject jsonNodes = service.client.searchOrders(vo);
        System.out.println("jsonNodes = " + jsonNodes);
    }


    @Test
    void getEndOrderIds() {
        // 使用日本时区 (Asia/Tokyo) 来解析 startDatetime 和 endDatetime
        ZonedDateTime startDatetime = LocalDateTimeUtil.parse("2024-10-01 00:00:00", "yyyy-MM-dd HH:mm:ss")
                .atZone(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime endDatetime = LocalDateTimeUtil.parse("2024-12-01 00:00:00", "yyyy-MM-dd HH:mm:ss")
                .atZone(ZoneId.of("Asia/Tokyo"));

        OrderSearchRequestVO vo = OrderSearchRequestVO.builder()
                .dateType(3)
                .startDatetime(startDatetime)
                .endDatetime(endDatetime)
                .build();
        JSONObject jsonNodes = service.client.searchOrders(vo);
        System.out.println("jsonNodes = " + jsonNodes);

    }
}