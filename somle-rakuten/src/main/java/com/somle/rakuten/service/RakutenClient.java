package com.somle.rakuten.service;

import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.RequestX;
import com.somle.framework.common.util.web.WebUtils;
import com.somle.rakuten.model.polo.RakutenTokenEntity;
import com.somle.rakuten.model.vo.OrderRequestVO;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.tomcat.util.codec.binary.Base64;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RakutenClient {
    RakutenTokenEntity entity;
    String accessToken;
    @Setter
    private OkHttpClient webClient;


    public RakutenClient(RakutenTokenEntity entity) {
        this.entity = entity;
        this.accessToken = getAuthorization(entity);
        this.webClient = new OkHttpClient();
    }

    /**
     * 对密钥进行编码
     */
    private String getAuthorization(RakutenTokenEntity entity) {
        String str = entity.getServiceSecret() + ":" + entity.getLicenseKey();
        return "ESA  " + Base64.encodeBase64String(str.getBytes());
    }


    @SneakyThrows
    public Object getOrders(OrderRequestVO orderRequestVO) {
        String endpoint = "/es/2.0/order/searchOrder/";
        String BASE_URL = "https://webservice.rms.rakuten.co.jp/";
        RequestX request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(BASE_URL + endpoint)
                .headers(getHeaders())
                .payload(orderRequestVO)
                .build();
        String bodyString = sendRequest(request).body().string();
        return JsonUtils.parseObject(bodyString, JSONObject.class);
    }

    public Object searchOrders() {
        //TODO 搜索时间范围内的订单号
        return null;
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", accessToken);
        headers.put("Content-Type", "application/json; charset=utf-8");
        return headers;
    }

    @SneakyThrows
    private Response sendRequest(RequestX request) {
        // Define the proxy details
        return webClient.newCall(WebUtils.toOkHttp(request)).execute();
    }
}
