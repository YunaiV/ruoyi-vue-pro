package com.somle.shopee.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;

public class ShopeeApiClient {

    // 基础配置
    private static final String HOST = "https://partner.shopeemobile.com";
    private static final String ACCESS_TOKEN = "random_string";
    private static final int PARTNER_ID = 80001;
    private static final String PARTNER_KEY = "test....."; // 密钥字符串

    public static void main(String[] args) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;

        // ----------------------------
        // 调用 Shop Level API
        // ----------------------------
        int shopId = 209920;
        String shopPath = "/api/v2/example/shop_level/get";
        String shopSign = generateSignature(PARTNER_ID, shopPath, timestamp, ACCESS_TOKEN, String.valueOf(shopId));
        String shopUrl = buildUrl(shopPath, PARTNER_ID, timestamp, ACCESS_TOKEN, shopSign)
            + "&shop_id=" + shopId;
        sendPostRequest(shopUrl, null);

        // ----------------------------
        // 调用 Merchant Level API
        // ----------------------------
        int merchantId = 1234567;
        String merchantPath = "/api/v2/example/merchant_level/get";
        String merchantSign = generateSignature(PARTNER_ID, merchantPath, timestamp, ACCESS_TOKEN, String.valueOf(merchantId));
        String merchantUrl = buildUrl(merchantPath, PARTNER_ID, timestamp, ACCESS_TOKEN, merchantSign)
            + "&merchant_id=" + merchantId;
        sendPostRequest(merchantUrl, null);

        // ----------------------------
        // 调用 Public API (获取 access_token)
        // ----------------------------
        String publicPath = "/api/v2/auth/merchant/access_token/get";
        String publicSign = generatePublicSignature(PARTNER_ID, publicPath, timestamp);
        String publicUrl = HOST + publicPath
            + "?partner_id=" + PARTNER_ID
            + "&timestamp=" + timestamp
            + "&sign=" + publicSign;
        String publicBody = String.format(
            "{\"partner_id\":%d,\"merchant_id\":%d,\"refresh_token\":\"%s\"}",
            PARTNER_ID, merchantId, "testingtoken");
        sendPostRequest(publicUrl, publicBody);
    }

    // 生成常规签名
    private static String generateSignature(int partnerId, String path, long timestamp,
                                            String accessToken, String entityId) throws Exception {
        String baseString = String.format("%d%s%d%s%s", partnerId, path, timestamp, accessToken, entityId);
        return hmacSha256(PARTNER_KEY, baseString);
    }

    // 生成 Public API 签名
    private static String generatePublicSignature(int partnerId, String path, long timestamp) throws Exception {
        String baseString = String.format("%d%s%d", partnerId, path, timestamp);
        return hmacSha256(PARTNER_KEY, baseString);
    }

    // HMAC-SHA256 加密
    private static String hmacSha256(String key, String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(rawHmac);
    }

    // 字节转十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    // 构建基础 URL
    private static String buildUrl(String path, int partnerId, long timestamp, String accessToken, String sign) {
        return HOST + path
            + "?partner_id=" + partnerId
            + "&timestamp=" + timestamp
            + "&access_token=" + accessToken
            + "&sign=" + sign;
    }

    // 发送 POST 请求
    private static void sendPostRequest(String urlStr, String body) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            if (body != null) {
                try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                    dos.writeBytes(body);
                }
            }

            int responseCode = conn.getResponseCode();
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("Response Code: " + responseCode);
                System.out.println("Response Body: " + response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}