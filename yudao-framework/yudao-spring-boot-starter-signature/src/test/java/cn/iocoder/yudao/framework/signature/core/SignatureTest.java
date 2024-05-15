package cn.iocoder.yudao.framework.signature.core;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.signature.core.util.SignatureUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * {@link SignatureTest} 的单元测试
 */
public class SignatureTest {

    /**
     * 模拟客户端发起http请求，对参数加签
     *
     * @param args args
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        String appId = "zs001";
        Snowflake snowflake = new Snowflake();

        // 验签请求头前端需传入字段
        Map<String, String> headers = new HashMap<>();
        headers.put("appId", appId);
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
        headers.put("nonce", String.valueOf(snowflake.nextId()));

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
        Map bodyMap = JsonUtils.parseObject(body, Map.class);
        TreeMap traverseMap = SignatureUtils.traverseMap(bodyMap);

        SortedMap<String, String> paramsMap = new TreeMap<>();

        SortedMap<String, String> sortedMap = new TreeMap<>(headers);
        sortedMap.put("body", JsonUtils.toJsonString(traverseMap));
        // 客户端加签规则
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            builder.append(entry.getKey()).append(entry.getValue());
        }
        /**
         * url + appSecret
         * /admin-api/system/signature-test(接口名) 客户端不需要传递，但是可以用来加签
         * 如果url没有/{id}像这种动态参数的情况下，此字段可以不用加签，既客户端不需要拼接/admin-api/system/signature-test
         */
        builder.append("/admin-api/system/signature-testd3cbeed9baf4e68673a1f69a2445359a20022b7c28ea2933dd9db9f3a29f902b");
        System.out.println(builder);

        headers.put("sign", DigestUtil.sha256Hex(StringUtils.getBytes(builder.toString(), "UTF-8")));
        System.out.println("headers==" + JsonUtils.toJsonString(headers));

        HttpRequest post = HttpUtil.createPost("http://localhost:48080/admin-api/system/signature-test");
        post.addHeaders(headers);
        post.body(body);
        try (HttpResponse execute = post.execute()) {
            System.out.println("执行结果==" + execute);
        }
    }

}
