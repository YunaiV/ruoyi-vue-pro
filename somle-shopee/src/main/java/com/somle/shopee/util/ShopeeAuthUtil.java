package com.somle.shopee.util;


import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class ShopeeAuthUtil {


    //generate auth url
//    public static void shop_auth() {
//        long timest = System.currentTimeMillis() / 1000L;
//        String host = "https://partner.shopeemobile.com";
//        String path = "/api/v2/shop/auth_partner";
//        String redirect_url = "https://www.baidu.com/";
//        long partner_id = 123456L;
//        String tmp_partner_key = "...";
//        String tmp_base_string = String.format("%s%s%s", partner_id, path, timest);
//        byte[] partner_key;
//        byte[] base_string;
//        String sign = "";
//        try {
//            base_string = tmp_base_string.getBytes("UTF-8");
//            partner_key = tmp_partner_key.getBytes("UTF-8");
//            Mac mac = Mac.getInstance("HmacSHA256");
//            SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
//            mac.init(secret_key);
//            sign = String.format("%064x", new BigInteger(1, mac.doFinal(base_string)));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&redirect=%s", partner_id, timest, sign, redirect_url);
//        System.out.println(url);
//    }


    @SneakyThrows
    public static String getSignature(String partnerId, String apiPath, String accessToken, String shopId, String partnerKey, Long timestamp) {


        // 构建基础字符串
        String baseStr = partnerId + apiPath + timestamp + accessToken + shopId;

        // 生成 HMAC-SHA256 签名
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            partnerKey.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );
        sha256Hmac.init(secretKeySpec);

        byte[] hmacBytes = sha256Hmac.doFinal(
            baseStr.getBytes(StandardCharsets.UTF_8)
        );

        // 转换为十六进制小写字符串
        String sign = bytesToHex(hmacBytes);

        System.out.println("Generated Signature: " + sign);
        return sign;
    }


    // 字节数组转十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
