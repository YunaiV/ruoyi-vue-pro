package com.somle.manomano.service;


import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.somle.manomano.model.ManomanoShop;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@Slf4j
public class ManomanoClient {

    private ManomanoShop shop;

    public ManomanoClient(ManomanoShop shop) {
        this.shop = shop;
    }


    @SneakyThrows
    public JSONObject getOrders() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://partnersapi.manomano.com/orders/v1/orders?seller_contract_id=23005541")
                .method("GET", null)
                .addHeader("x-api-key", shop.getToken().getAppKey())
                .build();
        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        var result = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return result;
    }


}