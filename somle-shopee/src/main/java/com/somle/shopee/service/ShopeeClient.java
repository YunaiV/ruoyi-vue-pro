package com.somle.shopee.service;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.somle.shopee.model.ShopeeAccount;
import com.somle.shopee.model.reps.*;
import com.somle.shopee.util.ShopeeAuthUtil;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class ShopeeClient {

    private final String URL = "https://partner.shopeemobile.com";
    private final String response_optional_fields = "buyer_user_id,buyer_username,estimated_shipping_fee,recipient_address,actual_shipping_fee ,goods_to_declare,note,note_update_time,item_list,pay_time,dropshipper, dropshipper_phone,split_up,buyer_cancel_reason,cancel_by,cancel_reason,actual_shipping_fee_confirmed,buyer_cpf_id,fulfillment_flag,pickup_done_time,package_list,shipping_carrier,payment_method,total_amount,buyer_username,invoice_data,order_chargeable_weight_gram,return_request_due_date,edt";
    private ShopeeAccount account;

    public ShopeeClient(ShopeeAccount account) {
        this.account = account;
    }

    @SneakyThrows
    public void getRefreshToken(String code) {


        String url = "https://partner.shopeemobile.com/api/v2/auth/token/get";
        // 获取当前时间戳（秒级）
        long timestamp = Instant.now().getEpochSecond();
        String sign = ShopeeAuthUtil.getSignature(account.getPartnerId().toString(), "/api/v2/auth/token/get", "", account.getShopId().toString(), account.getPartnerKey(), timestamp);
        JSONObject payload = new JSONObject();
        payload.put("code", code);
        payload.put("partner_id", Integer.valueOf(account.getPartnerId().toString()));

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(payload));

//        var request = RequestX.builder()
//            .requestMethod(RequestX.Method.POST)
//            .url(url)
//            .headers(generateHeaders(sign, account.getPartnerId().toString(), String.valueOf(timestamp)))
//            .payload(payload)
//            .build();
//        var response = WebUtils.sendRequest(request);
//        String bodyString = response.body().string();
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .headers(Headers.of(generateHeaders(sign, account.getPartnerId().toString(), String.valueOf(timestamp))))
            .build();
        Response response = client.newCall(request).execute();
        String bodyString = response.body().string();
        System.out.println(bodyString);

    }

    private Map<String, String> generateHeaders(String sign, String partnerId, String timestamp) {
        var headers = Map.of("sign", sign, "partner_id", partnerId, "timestamp", timestamp);
        return headers;
    }


    /**
     * @Description: 获取刷新令牌和访问令牌 官方示例
     */
    @SneakyThrows
    public String[] getRefreshTokenAndAccessToken(String code, long partner_id, String tmp_partner_key, long shop_id) {
        String[] res = new String[2];
        long timest = System.currentTimeMillis() / 1000L;
        String host = "https://partner.shopeemobile.com";
        String path = "/api/v2/auth/token/get";
        String tmp_base_string = String.format("%s%s%s", partner_id, path, timest);
        byte[] partner_key;
        byte[] base_string;
        BigInteger sign = null;
        String result = "";
        try {
            base_string = tmp_base_string.getBytes("UTF-8");
            partner_key = tmp_partner_key.getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
            mac.init(secret_key);
            sign = new BigInteger(1, mac.doFinal(base_string));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s", partner_id, timest, String.format("%032x", sign));
        URL url = new URL(tmp_url);
        HttpURLConnection conn = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            Map<String, Object> map = new HashMap<>();
            map.put("code", code);
            map.put("shop_id", shop_id);
            map.put("partner_id", partner_id);
            String json = JSON.toJSONString(map);
            conn.connect();
            out = new PrintWriter(conn.getOutputStream());
            out.print(json);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
                result += line;
            }
            JSONObject jsonObject = JSONObject.parseObject(result);
            res[0] = (String) jsonObject.get("access_token");
            res[1] = (String) jsonObject.get("refresh_token");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return res;
    }


    /**
     * @Description: 通过刷新令牌获取访问令牌 官方示例
     */

    @SneakyThrows
    public String getAccessTokenByRefreshToken(String refresh_token, long partner_id, String tmp_partner_key, long shop_id) {
        String[] res = new String[2];
        long timest = System.currentTimeMillis() / 1000L;
        String host = "https://partner.shopeemobile.com";
        String path = "/api/v2/auth/access_token/get";
        String tmp_base_string = String.format("%s%s%s", partner_id, path, timest);
        byte[] partner_key;
        byte[] base_string;
        BigInteger sign = null;
        String result = "";
        try {
            base_string = tmp_base_string.getBytes("UTF-8");
            partner_key = tmp_partner_key.getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
            mac.init(secret_key);
            sign = new BigInteger(1, mac.doFinal(base_string));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s", partner_id, timest, String.format("%032x", sign));
        URL url = new URL(tmp_url);
        HttpURLConnection conn = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            Map<String, Object> map = new HashMap<>();
            map.put("refresh_token", refresh_token);
            map.put("shop_id", shop_id);
            map.put("partner_id", partner_id);
            String json = JSON.toJSONString(map);
            conn.connect();
            out = new PrintWriter(conn.getOutputStream());
            out.print(json);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
                result += line;
            }
            JSONObject jsonObject = JSONObject.parseObject(result);
            res[0] = (String) jsonObject.get("access_token");
            res[1] = (String) jsonObject.get("refresh_token");
            return res[0];
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return res[0];
    }

    @SneakyThrows
    public ShopeeShopReps getShop() {
        long timestamp = System.currentTimeMillis() / 1000L;
        String sign = ShopeeAuthUtil.getSignature(account.getPartnerId().toString(), "/api/v2/shop/get_shop_info", account.getAccessToken(), account.getShopId().toString(), account.getPartnerKey(), timestamp);

        String fullUrl = URL + "/api/v2/shop/get_shop_info?access_token=" + account.getAccessToken() +
            "&partner_id=" + account.getPartnerId() + "&shop_id=" + account.getShopId() + "&sign=" + sign + "&timestamp=" + String.valueOf(timestamp);
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(fullUrl)
            .build();
        var response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        ShopeeShopReps shopReps = JSON.parseObject(bodyString, ShopeeShopReps.class);
        return shopReps;
    }

    @SneakyThrows
    public ShopeeItemListReps getItemList(Integer offset) {
        var endPoint = "/api/v2/product/get_item_list";
        long timestamp = System.currentTimeMillis() / 1000L;
        String sign = ShopeeAuthUtil.getSignature(account.getPartnerId().toString(), endPoint, account.getAccessToken(), account.getShopId().toString(), account.getPartnerKey(), timestamp);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("partner_id", account.getPartnerId());
        jsonObject.put("access_token", account.getAccessToken());
        jsonObject.put("shop_id", account.getShopId());
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("offset", offset);
        jsonObject.put("sign", sign);
        jsonObject.put("page_size", 50);
        jsonObject.put("item_status", "NORMAL");
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(URL + endPoint)
            .queryParams(jsonObject)
            .build();
        var response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        ShopeeItemListReps shopeeItemListReps = JSON.parseObject(bodyString, ShopeeItemListReps.class);
        return shopeeItemListReps;
    }

    @SneakyThrows
    public ShopeeItemBaseInfoReps getItemBaseInfo(List<Long> itemIdList) {
        var endPoint = "/api/v2/product/get_item_base_info";
        long timestamp = System.currentTimeMillis() / 1000L;
        String sign = ShopeeAuthUtil.getSignature(account.getPartnerId().toString(), endPoint, account.getAccessToken(), account.getShopId().toString(), account.getPartnerKey(), timestamp);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("partner_id", account.getPartnerId());
        jsonObject.put("access_token", account.getAccessToken());
        jsonObject.put("shop_id", account.getShopId());
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("sign", sign);
        jsonObject.put("item_id_list", itemIdList);
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(URL + endPoint)
            .queryParams(jsonObject)
            .build();
        var response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        ShopeeItemBaseInfoReps shopeeItemBaseInfoReps = JSON.parseObject(bodyString, ShopeeItemBaseInfoReps.class);
        return shopeeItemBaseInfoReps;
    }

    @SneakyThrows
    public ShopeeModelListResp getModelList(Long itemId) {
        var endPoint = "/api/v2/product/get_model_list";
        long timestamp = System.currentTimeMillis() / 1000L;
        String sign = ShopeeAuthUtil.getSignature(account.getPartnerId().toString(), endPoint, account.getAccessToken(), account.getShopId().toString(), account.getPartnerKey(), timestamp);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("partner_id", account.getPartnerId());
        jsonObject.put("access_token", account.getAccessToken());
        jsonObject.put("shop_id", account.getShopId());
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("sign", sign);
        jsonObject.put("item_id", itemId);
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(URL + endPoint)
            .queryParams(jsonObject)
            .build();
        var response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        ShopeeModelListResp shopeeModelListResp = JSON.parseObject(bodyString, ShopeeModelListResp.class);
        return shopeeModelListResp;
    }


    public List<ShopeeItemBaseInfoReps.Response.Item> getAllProducts() {
        Integer offset = 0;
        List<ShopeeItemBaseInfoReps.Response.Item> products = new ArrayList<>();
        ShopeeItemListReps itemListReps = getItemList(offset);
        List<Long> itemIdList = itemListReps.getResponse().getItem().stream().map(ShopeeItemListReps.Response.Item::getItemId).toList();
        ShopeeItemBaseInfoReps itemBaseInfoReps = getItemBaseInfo(itemIdList);
        products.addAll(itemBaseInfoReps.getResponse().getItemList());

        while (itemListReps.getResponse().isHasNextPage()) {
            offset += 50;
            itemListReps = getItemList(offset);
            itemIdList = itemListReps.getResponse().getItem().stream().map(ShopeeItemListReps.Response.Item::getItemId).toList();
            itemBaseInfoReps = getItemBaseInfo(itemIdList);
            products.addAll(itemBaseInfoReps.getResponse().getItemList());
        }
        products.forEach(item -> item.setShopeeModelListResp(getModelList(item.getItemId())));
        return products;
    }

    @SneakyThrows
    public ShopeeOrderListReps getOrderList(Long timeFrom, Long timeTo, String cursor) {
        var endPoint = "/api/v2/order/get_order_list";
        long timestamp = System.currentTimeMillis() / 1000L;
        String sign = ShopeeAuthUtil.getSignature(account.getPartnerId().toString(), endPoint, account.getAccessToken(), account.getShopId().toString(), account.getPartnerKey(), timestamp);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("partner_id", account.getPartnerId());
        jsonObject.put("access_token", account.getAccessToken());
        jsonObject.put("shop_id", account.getShopId());
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("sign", sign);
        jsonObject.put("page_size", 50);
        jsonObject.put("time_range_field", "create_time");
        jsonObject.put("time_from", timeFrom);
        jsonObject.put("time_to", timeTo);
        jsonObject.put("cursor", cursor);
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(URL + endPoint)
            .queryParams(jsonObject)
            .build();
        var response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        ShopeeOrderListReps shopeeOrderListReps = JSONUtil.toBean(bodyString, ShopeeOrderListReps.class);
        return shopeeOrderListReps;
    }

    @SneakyThrows
    public List<ShopeeOrderDetailReps.Order> getOrderDetail(String orderSnList) {
        String endPoint = "/api/v2/order/get_order_detail";
        long timestamp = System.currentTimeMillis() / 1000L;
        String sign = ShopeeAuthUtil.getSignature(account.getPartnerId().toString(), endPoint, account.getAccessToken(), account.getShopId().toString(), account.getPartnerKey(), timestamp);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("partner_id", account.getPartnerId());
        jsonObject.put("access_token", account.getAccessToken());
        jsonObject.put("shop_id", account.getShopId());
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("sign", sign);
        jsonObject.put("order_sn_list", orderSnList);
        jsonObject.put("response_optional_fields", response_optional_fields);
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(URL + endPoint)
            .queryParams(jsonObject)
            .build();
        var response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        ShopeeOrderDetailReps shopeeOrderDetailReps = JSON.parseObject(bodyString, ShopeeOrderDetailReps.class);
        return shopeeOrderDetailReps.getResponse().getOrderList();
    }


    public List<ShopeeOrderDetailReps.Order> getAllOrders(Long timeFrom, Long timeTo) {
        List<ShopeeOrderDetailReps.Order> orders = new ArrayList<>();
        ShopeeOrderListReps orderList = getOrderList(timeFrom, timeTo, "");
        if (orderList.getResponse().getOrderList().isEmpty()) {
            return orders;
        }
        StringBuilder orderSnList = new StringBuilder();
        for (ShopeeOrderListReps.Response.Order order : orderList.getResponse().getOrderList()) {
            orderSnList.append(order.getOrderSn()).append(",");
        }
        orderSnList.deleteCharAt(orderSnList.length() - 1);
        List<ShopeeOrderDetailReps.Order> orderDetail = getOrderDetail(orderSnList.toString());
        orders.addAll(orderDetail);
        while (orderList.getResponse().isMore()) {
            orderList = getOrderList(timeFrom, timeTo, orderList.getResponse().getNextCursor());
            orderSnList = new StringBuilder();
            for (ShopeeOrderListReps.Response.Order order : orderList.getResponse().getOrderList()) {
                orderSnList.append(order.getOrderSn()).append(",");
            }
            orderSnList.deleteCharAt(orderSnList.length() - 1);
            orderDetail = getOrderDetail(orderSnList.toString());
            orders.addAll(orderDetail);
        }
        return orders;
    }
}