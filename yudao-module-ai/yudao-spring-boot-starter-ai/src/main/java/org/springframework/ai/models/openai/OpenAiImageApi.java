package org.springframework.ai.models.openai;

import cn.hutool.json.JSONUtil;
import org.springframework.ai.models.openai.api.OpenAiImageRequest;
import org.springframework.ai.models.openai.api.OpenAiImageResponse;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

/**
 * open ai image
 * <p>
 * author: fansili
 * time: 2024/3/17 09:53
 */
@Slf4j
public class OpenAiImageApi {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com";
    private String apiKey = "your-api-key";
    // 发送请求 webClient
    private final WebClient webClient;

    private CloseableHttpClient httpclient = HttpClients.createDefault();

    public OpenAiImageApi(String apiKey) {
        this.apiKey = apiKey;
        // 创建一个HttpClient实例并设置超时
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(300)) // 设置响应超时时间为30秒
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000 * 100); // 设置连接超时为5秒
        this.webClient = WebClient.builder()
                .baseUrl(DEFAULT_BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public OpenAiImageResponse createImage(OpenAiImageRequest request) {
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create(DEFAULT_BASE_URL.concat("/v1/images/generations")));
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + apiKey);
        httpPost.setEntity(new StringEntity(JsonUtils.toJsonString(request), "UTF-8"));

        CloseableHttpResponse response= null;
        try {
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String resultJson = EntityUtils.toString(entity);
            log.info("openai 图片生成结果: {}", resultJson);
            return JSONUtil.toBean(resultJson, OpenAiImageResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
//        String res = webClient.post()
//                .uri(uriBuilder -> uriBuilder.path("/v1/images/generations").build())
//                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer " + apiKey)
//                // 设置请求体（这里假设jsonStr是一个JSON格式的字符串）
//                .body(BodyInserters.fromValue(JacksonUtil.toJson(request)))
//                // 发送请求并获取响应体
//                .retrieve()
//                // 转换响应体为String类型
//                .bodyToMono(String.class)
//                .block();
    }
}
