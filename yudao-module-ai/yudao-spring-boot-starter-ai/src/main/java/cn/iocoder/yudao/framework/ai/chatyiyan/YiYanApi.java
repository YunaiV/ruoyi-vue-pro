package cn.iocoder.yudao.framework.ai.chatyiyan;

import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanAuthRes;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanChatCompletion;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanChatCompletionRequest;
import cn.iocoder.yudao.framework.ai.chatyiyan.exception.YiYanApiException;
import lombok.Data;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 文心一言
 * <p>
 * author: fansili
 * time: 2024/3/8 21:47
 */
@Data
public class YiYanApi {

    private static final String DEFAULT_BASE_URL = "https://aip.baidubce.com";

    private static final String AUTH_2_TOKEN_URI = "/oauth/2.0/token";

    public static final String DEFAULT_CHAT_MODEL = "ERNIE 4.0";

    // 获取access_token流程 https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Ilkkrb0i5
    private String appKey;
    private String secretKey;
    private String token;
    // token刷新时间(秒)
    private int refreshTokenSecondTime;
    // 发送请求 webClient
    private final WebClient webClient;
    // 使用的模型
    private YiYanChatModel useChatModel;

    public YiYanApi(String appKey, String secretKey, YiYanChatModel useChatModel, int refreshTokenSecondTime) {
        this.appKey = appKey;
        this.secretKey = secretKey;
        this.useChatModel = useChatModel;
        this.refreshTokenSecondTime = refreshTokenSecondTime;

        this.webClient = WebClient.builder()
                .baseUrl(DEFAULT_BASE_URL)
                .build();

        token = getToken();
    }

    private String getToken() {
        // 文档地址: https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Ilkkrb0i5
        ResponseEntity<YiYanAuthRes> response = this.webClient.post()
                .uri(uriBuilder -> uriBuilder.path(AUTH_2_TOKEN_URI)
                        .queryParam("grant_type", "client_credentials")
                        .queryParam("client_id", appKey)
                        .queryParam("client_secret", secretKey)
                        .build()
                )
                .retrieve()
                .toEntity(YiYanAuthRes.class)
                .block();
        // 检查请求状态
        if (HttpStatusCode.valueOf(200) != response.getStatusCode()) {
            throw new YiYanApiException("一言认证失败! api：https://aip.baidubce.com/oauth/2.0/token 请检查 client_id、client_secret 是否正确!");
        }
        YiYanAuthRes body = response.getBody();
        return body.getAccess_token();
    }

    public ResponseEntity<YiYanChatCompletion> chatCompletionEntity(YiYanChatCompletionRequest request) {
        // TODO: 2024/3/10 小范 这里错误信息返回的结构不一样
//        {"error_code":17,"error_msg":"Open api daily request limit reached"}
        return this.webClient.post()
                .uri(uriBuilder
                        -> uriBuilder.path(useChatModel.getUri())
                        .queryParam("access_token", token)
                        .build())
                .body(Mono.just(request), YiYanChatCompletionRequest.class)
                .retrieve()
                .toEntity(YiYanChatCompletion.class)
                .block();
    }

    public Flux<YiYanChatCompletion> chatCompletionStream(YiYanChatCompletionRequest request) {
        return this.webClient.post()
                .uri(uriBuilder
                        -> uriBuilder.path(useChatModel.getUri())
                        .queryParam("access_token", token)
                        .build())
                .body(Mono.just(request), YiYanChatCompletionRequest.class)
                .retrieve()
                .bodyToFlux(YiYanChatCompletion.class);
    }
}
