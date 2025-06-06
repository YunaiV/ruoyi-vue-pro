package com.somle.rakuten.service;


import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.test.core.ut.SomleBaseDbUnitTest;
import com.somle.rakuten.model.pojo.PaginationRequestModel;
import com.somle.rakuten.model.pojo.RakutenTokenEntityDO;
import com.somle.rakuten.model.reps.RakutenInventoryRepsVO;
import com.somle.rakuten.model.reps.RakutenSearchOrderRepsVO;
import com.somle.rakuten.model.req.RakutenInventoryReqVO;
import com.somle.rakuten.model.req.RakutenOrderReqVO;
import com.somle.rakuten.model.req.RakutenOrderSearchReqVO;
import com.somle.rakuten.repository.RakutenTokenRepository;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
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

        JSONObject jsonObject = service.rakutenClients.get(0).getOrder(vo);
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

        PaginationRequestModel paginationReqModel = PaginationRequestModel.builder()
            .requestPage(22)
            .requestRecordsAmount(23)
            .build();

        RakutenOrderSearchReqVO vo = RakutenOrderSearchReqVO.builder()
            .dateType(3)
            .startDatetime(startDatetime)
            .endDatetime(endDatetime)
            .paginationRequestModel(paginationReqModel)
            .build();
        service.rakutenClients.get(0).getAllOrders(startDatetime, endDatetime);
        System.out.println("jsonNodes = ");
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
        RakutenSearchOrderRepsVO jsonNodes = service.rakutenClients.get(0).searchOrder(vo);
        System.out.println("jsonNodes = " + jsonNodes);

    }

    @Test
    void getProduct() {
        service.rakutenClients.get(0).getAllProducts();
        System.out.println("product = ");
    }

    @Test
    void getOrder() {
        RakutenOrderReqVO vo = RakutenOrderReqVO.builder()
            .version(8)
            .orderNumberList(List.of("372286-20241210-0870710097"))
            .build();
//        service.rakutenClients.get(0).getAllOrders();
        System.out.println("order = ");
    }

    @Test
    void getAllOrders() {
        String param = "2024-10-01";
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);

        String startTime = targetDate.toString() + " 00:00:00";
        String endTime = targetDate.toString() + " 23:59:59";

        // 使用日本时区 (Asia/Tokyo) 来解析 startDatetime 和 endDatetime
        ZonedDateTime startDatetime = LocalDateTimeUtil.parse(startTime, "yyyy-MM-dd HH:mm:ss")
            .atZone(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime endDatetime = LocalDateTimeUtil.parse(endTime, "yyyy-MM-dd HH:mm:ss")
            .atZone(ZoneId.of("Asia/Tokyo"));
    }

    @Test
    void getAllOrders2() {
        List<RakutenInventoryReqVO> inventories = new ArrayList<>();
        String skuId = "F02M1443AF02M1443A";
        var vo = RakutenInventoryReqVO.builder().manageNumber("F02M1443A").variantId(skuId).build();
        inventories.add(vo);
        RakutenInventoryRepsVO inventoryRepsVO = service.rakutenClients.get(0).getInventory(inventories);
        System.out.println("inventoryRepsVO = " + inventoryRepsVO);
    }
}