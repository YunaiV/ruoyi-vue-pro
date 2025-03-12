package com.somle.rakuten.service;


import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.rakuten.model.pojo.RakutenTokenEntityDO;
import com.somle.rakuten.model.vo.RakutenOrderReqVO;
import com.somle.rakuten.model.vo.RakutenOrderSearchReqVO;
import com.somle.rakuten.repository.RakutenTokenRepository;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Disabled
@Slf4j
@Import({RakutenService.class})
class RakutenServiceTest extends SomleBaseDbUnitTest {

    @Resource
    RakutenService service;
    @Resource
    RakutenTokenRepository repository;

    @Test
    void getRakuten() {
        ArrayList<String> list = new ArrayList<>();
        list.add("372286-20241210-0870710097");

        RakutenOrderReqVO vo = RakutenOrderReqVO.builder()
                .version(8)
                .orderNumberList(list)
                .build();

        JSONObject jsonObject = service.client.getOrder(vo);
        System.out.println(jsonObject);
    }

    @Test
    void getRakutenToken() {
        List<RakutenTokenEntityDO> all = repository.findAll();
        System.out.println("all = " + all);
    }

    @Test
//    @Rollback(false)
//    @Transactional
    void generateOne() {
        List<RakutenTokenEntityDO> all = repository.findAll();
        if ((long) all.size() == 0) {
            RakutenTokenEntityDO entity = new RakutenTokenEntityDO();
            entity.setServiceSecret("SP372286_XUye81AijnBIAYVK");
            entity.setLicenseKey("SL372286_XhRtwKoTGQyurnBF");
            RakutenTokenEntityDO save = repository.save(entity);
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

        // 构造请求体（RakutenOrderRequestVO）
        RakutenOrderReqVO vo = RakutenOrderReqVO.builder()
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
        System.out.println("response.body() = " + JSONUtil.toBean(response.body(), JSONObject.class));
    }

    @Test
    void searchOrder() {
        // 使用日本时区 (Asia/Tokyo) 来解析 startDatetime 和 endDatetime
        ZonedDateTime startDatetime = LocalDateTimeUtil.parse("2024-10-01 00:00:00", "yyyy-MM-dd HH:mm:ss")
                .atZone(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime endDatetime = LocalDateTimeUtil.parse("2024-12-01 00:00:00", "yyyy-MM-dd HH:mm:ss")
                .atZone(ZoneId.of("Asia/Tokyo"));

        RakutenOrderSearchReqVO vo = RakutenOrderSearchReqVO.builder()
                .dateType(3)
                .startDatetime(startDatetime)
                .endDatetime(endDatetime)
                .build();
        JSONObject jsonNodes = service.client.searchOrder(vo);
        System.out.println("jsonNodes = " + jsonNodes);
    }


    @Test
    void getEndOrderIds() {
        // 使用日本时区 (Asia/Tokyo) 来解析 startDatetime 和 endDatetime
        ZonedDateTime startDatetime = LocalDateTimeUtil.parse("2024-10-01 00:00:00", "yyyy-MM-dd HH:mm:ss")
                .atZone(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime endDatetime = LocalDateTimeUtil.parse("2024-12-01 00:00:00", "yyyy-MM-dd HH:mm:ss")
                .atZone(ZoneId.of("Asia/Tokyo"));

        RakutenOrderSearchReqVO vo = RakutenOrderSearchReqVO.builder()
                .dateType(3)
                .startDatetime(startDatetime)
                .endDatetime(endDatetime)
                .build();
        JSONObject jsonNodes = service.client.searchOrder(vo);
        System.out.println("jsonNodes = " + jsonNodes);

    }
}