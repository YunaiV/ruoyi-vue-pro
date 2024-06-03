package cn.iocoder.yudao.framework.signature.core;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * {@link SignatureTest} 的单元测试
 */
public class SignatureTest {

    @Test
    public void testSignatureGet() {
        String appId = "xxxxxx";
        Snowflake snowflake = new Snowflake();

        // 验签请求头前端需传入字段
        SortedMap<String, String> headersMap = new TreeMap<>();
        headersMap.put("appId", appId);
        headersMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        headersMap.put("nonce", String.valueOf(snowflake.nextId()));

        // 客户端加签内容
        StringBuilder clientSignatureContent = new StringBuilder();
        // 请求头
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            clientSignatureContent.append(entry.getKey()).append(entry.getValue());
        }
        // 请求 url
        clientSignatureContent.append("/admin-api/infra/demo01-contact/get");
        // 请求参数
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("id", "100");
        paramsMap.put("name", "张三");
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            queryString.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        clientSignatureContent.append(queryString.substring(1));
        // 密钥
        clientSignatureContent.append("d3cbeed9baf4e68673a1f69a2445359a20022b7c28ea2933dd9db9f3a29f902b");
        System.out.println("加签内容：" + clientSignatureContent);
        // 加签
        headersMap.put("sign", DigestUtil.sha256Hex(clientSignatureContent.toString()));
        headersMap.put("Authorization", "Bearer xxx");

        HttpRequest get = HttpUtil.createGet("http://localhost:48080/admin-api/infra/demo01-contact/get?id=100&name=张三");
        get.addHeaders(headersMap);
        System.out.println("执行结果==" + get.execute());
    }

    @Test
    public void testSignaturePost() {
        String appId = "xxxxxx";
        Snowflake snowflake = new Snowflake();

        // 验签请求头前端需传入字段
        SortedMap<String, String> headersMap = new TreeMap<>();
        headersMap.put("appId", appId);
        headersMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        headersMap.put("nonce", String.valueOf(snowflake.nextId()));

        // 客户端加签内容
        StringBuilder clientSignatureContent = new StringBuilder();
        // 请求头
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            clientSignatureContent.append(entry.getKey()).append(entry.getValue());
        }
        // 请求 url
        clientSignatureContent.append("/admin-api/infra/demo01-contact/create");
        // 请求体
        String body = "{\n" +
                "    \"password\": \"1\",\n" +
                "    \"date\": \"2024-04-24 16:28:00\",\n" +
                "    \"user\": {\n" +
                "        \"area\": \"浦东新区\",\n" +
                "        \"1\": \"xx\",\n" +
                "        \"2\": \"xx\",\n" +
                "        \"province\": \"上海市\",\n" +
                "        \"data\": {\n" +
                "            \"99\": \"xx\",\n" +
                "            \"1\": \"xx\",\n" +
                "            \"100\": \"xx\",\n" +
                "            \"2\": \"xx\",\n" +
                "            \"3\": \"xx\",\n" +
                "            \"array\": [\n" +
                "                {\n" +
                "                    \"3\": \"aa\",\n" +
                "                    \"4\": \"aa\",\n" +
                "                    \"2\": \"aa\",\n" +
                "                    \"1\": \"aa\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"99\": \"aa\",\n" +
                "                    \"100\": \"aa\",\n" +
                "                    \"88\": \"aa\",\n" +
                "                    \"120\": \"aa\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        \"sex\": \"男\",\n" +
                "        \"name\": \"张三\",\n" +
                "        \"array\": [\n" +
                "            \"1\",\n" +
                "            \"3\",\n" +
                "            \"5\",\n" +
                "            \"2\"\n" +
                "        ]\n" +
                "    },\n" +
                "    \"username\": \"xiaoming\"\n" +
                "}";
        clientSignatureContent.append(body);

        // 密钥
        clientSignatureContent.append("d3cbeed9baf4e68673a1f69a2445359a20022b7c28ea2933dd9db9f3a29f902b");
        System.out.println("加签内容：" + clientSignatureContent);
        // 加签
        headersMap.put("sign", DigestUtil.sha256Hex(clientSignatureContent.toString()));
        headersMap.put("Authorization", "Bearer xxx");

        HttpRequest post = HttpUtil.createPost("http://localhost:48080/admin-api/infra/demo01-contact/create");
        post.addHeaders(headersMap);
        post.body(body);
        try (HttpResponse execute = post.execute()) {
            System.out.println("执行结果==" + execute);
        }
    }

}
