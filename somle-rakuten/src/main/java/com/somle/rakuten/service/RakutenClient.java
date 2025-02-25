package com.somle.rakuten.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.rakuten.enums.RakutenOrderStatusEnum;
import com.somle.rakuten.model.pojo.RakutenTokenEntityDO;
import com.somle.rakuten.model.vo.RakutenOrderReqVO;
import com.somle.rakuten.model.vo.RakutenOrderSearchReqVO;
import com.somle.rakuten.util.ZonedDateTimeConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

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

    public RakutenClient(RakutenTokenEntityDO entity) {
        this.accessToken = generateAuthorization(entity);
    }

    /**
     * 对密钥进行编码
     */
    private String generateAuthorization(RakutenTokenEntityDO entity) {
        //数据库密钥过期了
        if (entity.getExpires().before(DateUtil.date().toJdkDate())) {
            throw new RuntimeException("Rakuten 密钥过期了");
        }
        String credentials = entity.getServiceSecret() + ":" + entity.getLicenseKey();
        //↓有个空格
        return "ESA" + " " + Base64.encode(credentials.getBytes());
    }


    @SneakyThrows
    public JSONObject getOrder(RakutenOrderReqVO vo) {
        String endpoint = "/es/2.0/order/getOrder/";
        return sendRequestAndParse(endpoint, JsonUtilsX.toJsonString(vo));
    }

    @SneakyThrows
    public JSONObject searchOrder(RakutenOrderSearchReqVO vo) {
        String endpoint = "/es/2.0/order/searchOrder/";
        isValidDateRange(vo.getStartDatetime().toLocalDateTime(), vo.getEndDatetime().toLocalDateTime());
        return sendRequestAndParse(endpoint, convertToJson(vo).toString());
    }

    @SneakyThrows
    public JSONObject getEndOrderIds(RakutenOrderSearchReqVO vo) {
        //检验日期范围
        isValidDateRange(vo.getStartDatetime().toLocalDateTime(), vo.getEndDatetime().toLocalDateTime());
        ArrayList<Integer> list = new ArrayList<>();
        list.add(RakutenOrderStatusEnum.SHIPPED.getCode());
        list.add(RakutenOrderStatusEnum.PAYMENT_COMPLETED.getCode());
        vo.setOrderProgressList(list);

        return sendRequestAndParse("/es/2.0/order/searchOrder/", convertToJson(vo).toString());

    }

    private Response sendPostRequest(String endpoint, String requestBody) {
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(BASE_URL + endpoint)
            .headers(getHeaders())
            .payload(JsonUtilsX.parseObject(requestBody, JSONObject.class))
            .build();
        return WebUtils.sendRequest(request);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        headers.put("Content-Type", "application/json; charset=utf-8");
        return headers;
    }


    private void isValidDateRange(LocalDateTime startDatetime, LocalDateTime endDatetime) {
        // 确保 endDatetime 大于 startDatetime
        LocalDateTimeUtils.isValidDateRange(startDatetime, endDatetime);
        // 判断是否在63天内
        LocalDateTimeUtils.isValidDateRange(startDatetime, endDatetime, 63);
        //判断开始日期是否截至今天(日本时间)在730日内
        LocalDateTimeUtils.isValidDateRange(startDatetime, ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).toLocalDateTime(), 730);
    }

    private JSONObject convertToJson(RakutenOrderSearchReqVO vo) {
        // 将 ZonedDateTime 转换成 String 格式
        String startDatetimeStr = vo.getStartDatetime() != null ? ZonedDateTimeConverter.convertToString(vo.getStartDatetime()) : null;
        String endDatetimeStr = vo.getEndDatetime() != null ? ZonedDateTimeConverter.convertToString(vo.getEndDatetime()) : null;
        // 创建一个临时对象来存储转换后的数据
        JSONObject jsonObject = JsonUtilsX.toJSONObject(vo);
        // 手动设置日期字段为字符串格式
        jsonObject.put("startDatetime", startDatetimeStr);
        jsonObject.put("endDatetime", endDatetimeStr);
        return jsonObject;
    }


    private JSONObject sendRequestAndParse(String endpoint, String requestBody) {
        try (Response response = sendPostRequest(endpoint, requestBody)) {
            if (response.body() == null) {
                log.error("Response body is null for endpoint: {}", endpoint);
                throw new RuntimeException("Response body is null");
            }
            return JsonUtilsX.parseObject(response.body().string(), JSONObject.class);
        } catch (Exception e) {
            log.error("Error occurred while sending request to endpoint: {}", endpoint, e);
            throw new RuntimeException("Error occurred while sending request", e);
        }
    }
}
