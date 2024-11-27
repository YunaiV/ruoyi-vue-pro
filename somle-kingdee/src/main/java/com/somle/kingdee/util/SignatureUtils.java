package com.somle.kingdee.util;

import com.somle.kingdee.model.KingdeeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @className: SignatureUtils
 * @author: Wqh
 * @date: 2024/10/23 15:01
 * @Version: 1.0
 * @description: 签名工具类
 */
public class SignatureUtils {
    private SignatureUtils(){}
    private final static String HMACSHA256 = "HmacSHA256";
    private static final String CLIENT_SECRET = "b5639a677545e611a297d6537f2b444c";
    public static final String CLIENT_ID = "240474";
    public static final String BASE_HOST = "https://api.kingdee.com";

    public static byte[] hmac256(String secret, String data) {
        try {
            // Create a new SecretKeySpec
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), HMACSHA256);
            // Get an instance of Mac and initialize with the secret key
            Mac mac = Mac.getInstance(HMACSHA256);
            mac.init(secretKeySpec);
            // Compute the HMAC SHA-256
            return mac.doFinal(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMACSHA256", e);
        }
    }

    public static String urlEncode(String str) {
        // log.debug(str);
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    public static String getApiString(String reqMtd, String urlPath, Map<String, String> params, String nonce,
                                      String timestamp) {
        String paramsStr = params.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + urlEncode(urlEncode(entry.getValue())))
                .collect(Collectors.joining("&"));
        String urlPathStr;
        urlPathStr = URLEncoder.encode(urlPath, StandardCharsets.UTF_8);

        return String.format("%s\n%s\n%s\nx-api-nonce:%s\nx-api-timestamp:%s\n",
                reqMtd, urlPathStr, paramsStr, nonce, timestamp);
    }

    public static String getApiSignature(String reqMtd, String urlPath, Map<String, String> params, String timestamp) {
        return getApiSignature(reqMtd, urlPath, params, timestamp, timestamp);
    }

    public static String getApiSignature(String reqMtd, String urlPath, Map<String, String> params, String nonce,
                                         String timestamp) {
        String apiString = getApiString(reqMtd, urlPath, params, nonce, timestamp);
        String apiStringToHmac256EnHex = Hex.encodeHexString(hmac256(CLIENT_SECRET, apiString));
        return Base64.encodeBase64String(apiStringToHmac256EnHex.getBytes());
    }

    public static String getAppSignature(KingdeeToken token) {
        String appSecret = token.getAppSecret();
        String appKey = token.getAppKey();
        String appKeyToHmac256EnHex = Hex.encodeHexString(hmac256(appSecret, appKey));
        return Base64.encodeBase64String(appKeyToHmac256EnHex.getBytes());
    }

    public static Map<String,String> getAuthHeaders(String ctime, String apiSignature) {
        return Map.of(
                "Content-Type", "application/json",
                "X-Api-Auth-Version", "2.0",
                "X-Api-ClientID", CLIENT_ID,
                "X-Api-Nonce", ctime,
                "X-Api-SignHeaders", "X-Api-TimeStamp,X-Api-Nonce",
                "X-Api-Signature", apiSignature,
                "X-Api-TimeStamp", ctime
        );
    }

    public static Map<String,String> getApiHeaders(String cts , String signature,String appToken) {
        return Map.of(
                "Content-Type", "application/json;charset=utf-8",
                "X-Api-ClientID", CLIENT_ID,
                "X-Api-Auth-Version", "2.0",
                "X-Api-TimeStamp", cts,
                "X-Api-SignHeaders", "X-Api-TimeStamp,X-Api-Nonce",
                "X-Api-Nonce", cts,
                "X-Api-Signature", signature,
                "app-token", appToken,
                "X-GW-Router-Addr", "https://tf.jdy.com"
        );
    }

}
