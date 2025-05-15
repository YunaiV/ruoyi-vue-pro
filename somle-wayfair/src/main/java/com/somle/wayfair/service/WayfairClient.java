package com.somle.wayfair.service;


import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.wayfair.model.WayfairToken;
import com.somle.wayfair.model.reps.WayfairOrderRepsVO;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.mountcloud.graphql.request.query.DefaultGraphqlQuery;
import org.mountcloud.graphql.request.result.ResultAttributtes;

import java.time.LocalDateTime;

@Slf4j
@Getter
public class WayfairClient {

    private final WayfairToken token;

    private String accessToken;

    private final String url;

    public WayfairClient(WayfairToken token) {
        this.token = token;
        this.url = "https://api.wayfair.com/";
        refreshAccessToken();
    }

    @SneakyThrows
    public void refreshAccessToken() {
        OkHttpClient client = new OkHttpClient();

        // Create the JSON body using JSONObject
        JSONObject json = new JSONObject();
        json.put("client_id", token.getClientId());
        json.put("client_secret", token.getClientSecret());
        json.put("audience", url);
        json.put("grant_type", "client_credentials");

        // Create the request body
        RequestBody requestBody = RequestBody.create(
            json.toString(),
            MediaType.get("application/json; charset=utf-8")
        );

        // Build the request
        Request request = new Request.Builder()
            .url("https://sso.auth.wayfair.com/oauth/token")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build();

        // Execute the request
        Response response = client.newCall(request).execute();

        accessToken = WebUtils.parseResponse(response, JSONObject.class).getString("access_token");

    }

    @SneakyThrows
    public WayfairOrderRepsVO getOrders(LocalDateTime fromDate) {
        DefaultGraphqlQuery query = new DefaultGraphqlQuery("getDropshipPurchaseOrders");

        query.addParameter("fromDate", fromDate).addParameter("limit", Integer.MAX_VALUE);
        query.addResultAttributes(
            "poNumber",
            "poDate",
            "orderId",
            "estimatedShipDate",
            "customerName",
            "customerAddress1",
            "customerAddress2",
            "customerCity",
            "customerState",
            "customerPostalCode",
            "orderType",
            "packingSlipUrl"
        );
        ResultAttributtes shippingInfo = new ResultAttributtes("shippingInfo");
        shippingInfo.addResultAttributes("shipSpeed", "carrierCode");

        ResultAttributtes warehouse = new ResultAttributtes("warehouse");
        warehouse.addResultAttributes("id", "name");

        ResultAttributtes address = new ResultAttributtes("address");
        address.addResultAttributes("name", "address1", "address2", "address3", "city", "state", "country", "postalCode");
        warehouse.addResultAttributes(address);

        ResultAttributtes products = new ResultAttributtes("products");
        products.addResultAttributes("partNumber", "quantity", "price");

        ResultAttributtes shipTo = new ResultAttributtes("shipTo");
        shipTo.addResultAttributes("name", "address1", "address2", "address3", "city", "state", "country", "postalCode", "phoneNumber");

        query.addResultAttributes(shippingInfo);
        query.addResultAttributes(warehouse);
        query.addResultAttributes(products);
        query.addResultAttributes(shipTo);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, query.toString());

        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("Accept", "application/json");
        headerBuilder.add("Content-type", "application/json");
        headerBuilder.add("Authorization", "Bearer " + accessToken);
        Request request = new Request.Builder()
            .url("https://api.wayfair.com/v1/graphql")
            .method("POST", requestBody)
            .headers(headerBuilder.build())
            .build();
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        WayfairOrderRepsVO wayfairOrderRepsVO = JSONUtil.toBean(result, WayfairOrderRepsVO.class);
        return wayfairOrderRepsVO;
    }
}
