//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.somle.lazada.sdk.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class LazopUtils {
    private static String intranetIp;

    private LazopUtils() {
    }

    public static String signApiRequest(RequestContext requestContext, String appSecret, String signMethod) throws IOException {
        return signApiRequest(requestContext.getApiName(), requestContext.getAllParams(), (String) null, appSecret, signMethod);
    }

    public static String signApiRequest(String apiName, Map<String, String> params, String body, String appSecret, String signMethod) throws IOException {
        String[] keys = (String[]) params.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        StringBuilder query = new StringBuilder();
        query.append(apiName);

        for (String key : keys) {
            String value = (String) params.get(key);
            if (areNotEmpty(key, value)) {
                query.append(key).append(value);
            }
        }

        if (body != null) {
            query.append(body);
        }

        byte[] bytes = null;
        if (signMethod.equals("sha256")) {
            bytes = encryptHMACSHA256(query.toString(), appSecret);
            return byte2hex(bytes);
        } else {
            throw new IOException("Invalid Sign Method");
        }
    }

    private static byte[] encryptHMACSHA256(String data, String secret) throws IOException {
        byte[] bytes = null;

        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            bytes = mac.doFinal(data.getBytes("UTF-8"));
            return bytes;
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse.toString());
        }
    }

    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();

        for (int i = 0; i < bytes.length; ++i) {
            String hex = Integer.toHexString(bytes[i] & 255);
            if (hex.length() == 1) {
                sign.append("0");
            }

            sign.append(hex.toUpperCase());
        }

        return sign.toString();
    }

    public static <V> Map<String, V> cleanupMap(Map<String, V> map) {
        if (map != null && !map.isEmpty()) {
            Map<String, V> result = new HashMap(map.size());

            for (Map.Entry<String, V> entry : map.entrySet()) {
                if (entry.getValue() != null) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }

            return result;
        } else {
            return null;
        }
    }

    public static String getIntranetIp() {
        if (intranetIp == null) {
            try {
                intranetIp = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception var1) {
                intranetIp = "127.0.0.1";
            }
        }

        return intranetIp;
    }

    public static boolean isEmpty(String value) {
        int strLen;
        if (value != null && (strLen = value.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(value.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean areNotEmpty(String... values) {
        boolean result = true;
        if (values != null && values.length != 0) {
            for (String value : values) {
                result &= !isEmpty(value);
            }
        } else {
            result = false;
        }

        return result;
    }

    public static String formatDateTime(Date date, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
}
