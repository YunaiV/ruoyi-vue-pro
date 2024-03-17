package cn.iocoder.yudao.framework.ai.imageopenai;

import cn.iocoder.yudao.framework.ai.imageopenai.api.OpenAiImageRequest;
import cn.iocoder.yudao.framework.ai.imageopenai.api.OpenAiImageResponse;
import cn.iocoder.yudao.framework.ai.util.JacksonUtil;
import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * open ai image
 * <p>
 * author: fansili
 * time: 2024/3/17 09:53
 */
public class OpenAiImageApi {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com";
    private String apiKey = "your-api-key";
    // 发送请求 webClient
    private final WebClient webClient;

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
        String res = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/v1/images/generations").build())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                // 设置请求体（这里假设jsonStr是一个JSON格式的字符串）
                .body(BodyInserters.fromValue(JacksonUtil.toJson(request)))
                // 发送请求并获取响应体
                .retrieve()
                // 转换响应体为String类型
                .bodyToMono(String.class)
                .block();
        // TODO: 2024/3/17 这里发送请求会失败！
        return null;
    }
}
