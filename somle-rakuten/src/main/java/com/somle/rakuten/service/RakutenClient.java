package com.somle.rakuten.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.rakuten.enums.OrderStatusEnum;
import com.somle.rakuten.model.polo.RakutenTokenEntity;
import com.somle.rakuten.model.vo.OrderRequestVO;
import com.somle.rakuten.model.vo.OrderSearchRequestVO;
import com.somle.rakuten.utill.ZonedDateTimeConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RakutenClient {
    private final String accessToken;
    private static final String BASE_URL = "https://api.rms.rakuten.co.jp";

    public RakutenClient(RakutenTokenEntity entity) {
        this.accessToken = generateAuthorization(entity);
    }

    /**
     * 对密钥进行编码
     */
    private String generateAuthorization(RakutenTokenEntity entity) {
        String credentials = entity.getServiceSecret() + ":" + entity.getLicenseKey();
        //↓有个空格
        return "ESA" + " " + Base64.encode(credentials.getBytes());
    }


    @SneakyThrows
    public JSONObject getOrders(OrderRequestVO vo) {
        String endpoint = "/es/2.0/order/getOrder/";
        try (HttpResponse response = sendPostRequest(endpoint, JSONUtil.toJsonStr(vo))) {
            return JsonUtils.parseObject(response.body(), JSONObject.class);
        }
    }

    @SneakyThrows
    public JSONObject searchOrders(OrderSearchRequestVO vo) {
        String endpoint = "/es/2.0/order/searchOrder/";
        //校验时间范围
        isValidDateRange(vo.getStartDatetime().toLocalDateTime(), vo.getEndDatetime().toLocalDateTime());
        try (HttpResponse response = sendPostRequest(endpoint, convertToJson(vo).toString())) {
            return JsonUtils.parseObject(response.body(), JSONObject.class);
        }
    }

    private HttpResponse sendPostRequest(String endpoint, String requestBody) {
        return HttpRequest.post(BASE_URL + endpoint)
                .headerMap(getHeaders(), true)
                .body(requestBody)
                .timeout(2 * 1000)
                .execute();
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        headers.put("Content-Type", "application/json; charset=utf-8");
        return headers;
    }


    private void isValidDateRange(LocalDateTime startDatetime, LocalDateTime endDatetime) {
        // 确保 endDatetime 大于 startDatetime
        if (endDatetime.isBefore(startDatetime) || endDatetime.isEqual(startDatetime)) {
            throw new IllegalArgumentException("结束时间必须大于开始时间");
        }

        // 计算日期之间的天数差
        long days = LocalDateTimeUtil.between(startDatetime, endDatetime).toDays();
        // 判断是否在63天内
        if (days > 63) {
            throw new IllegalArgumentException("日期范围超出了63天");
        }
        //判断开始日期是否截至今天(日本时间)在730日内
        long days2 = LocalDateTimeUtil.between(startDatetime, ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).toLocalDateTime()).toDays();
        if (days2 > 730) {
            throw new IllegalArgumentException("开始日期范围超出了730天");
        }

    }

    private cn.hutool.json.JSONObject convertToJson(OrderSearchRequestVO vo) {
        // 将 ZonedDateTime 转换成 String 格式
        String startDatetimeStr = vo.getStartDatetime() != null ? ZonedDateTimeConverter.convertToString(vo.getStartDatetime()) : null;
        String endDatetimeStr = vo.getEndDatetime() != null ? ZonedDateTimeConverter.convertToString(vo.getEndDatetime()) : null;
        // 创建一个临时对象来存储转换后的数据
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(vo);
        // 手动设置日期字段为字符串格式
        jsonObject.set("startDatetime", startDatetimeStr);
        jsonObject.set("endDatetime", endDatetimeStr);
        return jsonObject;
    }

    @SneakyThrows
    public JSONObject getEndOrderIds(OrderSearchRequestVO vo) {
        String endpoint = "/es/2.0/order/searchOrder/";
        //校验日期范围
        isValidDateRange(vo.getStartDatetime().toLocalDateTime(), vo.getEndDatetime().toLocalDateTime());

        ArrayList<Integer> list = new ArrayList<>();
        list.add(OrderStatusEnum.SHIPPED.getCode());
        list.add(OrderStatusEnum.PAYMENT_COMPLETED.getCode());
        vo.setOrderProgressList(list);
        try (HttpResponse response = sendPostRequest(endpoint, convertToJson(vo).toString())) {
            return JsonUtils.parseObject(response.body(), JSONObject.class);
        }
    }
}
