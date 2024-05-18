package cn.iocoder.yudao.framework.ai.core.model.yiyan.api;

import cn.iocoder.yudao.framework.ai.core.model.yiyan.exception.YiYanApiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 文心一言 API
 *
 * @author fansili
 */
public class YiYanApi {

    private static final String DEFAULT_BASE_URL = "https://aip.baidubce.com";

    private static final String AUTH_2_TOKEN_URI = "/oauth/2.0/token";

    public static final String DEFAULT_CHAT_MODEL = YiYanChatModel.ERNIE4_0.getModel();

    private final String appKey;
    private final String secretKey;
    /**
     * TODO fan：这个是不是要有个刷新机制哈；如果目前不需要，可以删除掉 refreshTokenSecondTime；整体更简洁；
     */
    private final String token;
    /**
     * token 刷新时间(秒)
     */
    private int refreshTokenSecondTime;
    /**
     * 发送请求 webClient
     */
    private final WebClient webClient;
    /**
     * 使用的模型
     */
    private final YiYanChatModel useChatModel;

    public YiYanApi(String appKey, String secretKey, YiYanChatModel useChatModel, int refreshTokenSecondTime) {
        this.appKey = appKey;
        this.secretKey = secretKey;
        this.useChatModel = useChatModel;
        this.refreshTokenSecondTime = refreshTokenSecondTime;
        this.webClient = WebClient.builder().baseUrl(DEFAULT_BASE_URL).build();
        // 获取访问令牌
        token = getToken();
    }

    /**
     * 获得访问令牌
     *
     * @see <a href="https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Ilkkrb0i5">文档地址</>
     * @return 访问令牌
     */
    private String getToken() {
        ResponseEntity<YiYanAuthResponse> response = this.webClient.post()
                .uri(uriBuilder -> uriBuilder.path(AUTH_2_TOKEN_URI)
                        .queryParam("grant_type", "client_credentials")
                        .queryParam("client_id", appKey)
                        .queryParam("client_secret", secretKey)
                        .build()
                )
                .retrieve()
                .toEntity(YiYanAuthResponse.class)
                .block();
        // 检查请求状态
        // TODO @fan：可以使用 response.getStatusCode().is2xxSuccessful()
        if (HttpStatusCode.valueOf(200) != response.getStatusCode()
            || response.getBody() == null) {
            // TODO @fan：可以使用 IllegalStateException 替代；另外，最好打印下返回；方便排错；
            throw new YiYanApiException("一言认证失败! api：https://aip.baidubce.com/oauth/2.0/token 请检查 client_id、client_secret 是否正确!");
        }
        return response.getBody().getAccess_token();
    }

    public ResponseEntity<YiYanChatCompletionResponse> chatCompletionEntity(YiYanChatCompletionRequest request) {
        // TODO: 2024/3/10 小范 这里错误信息返回的结构不一样
//        {"error_code":17,"error_msg":"Open api daily request limit reached"}
        return this.webClient.post()
                .uri(uriBuilder
                        -> uriBuilder.path(useChatModel.getUri())
                        .queryParam("access_token", token)
                        .build())
                .body(Mono.just(request), YiYanChatCompletionRequest.class)
                .retrieve()
                .toEntity(YiYanChatCompletionResponse.class)
                .block();
    }

    public Flux<YiYanChatCompletionResponse> chatCompletionStream(YiYanChatCompletionRequest request) {
        return this.webClient.post()
                .uri(uriBuilder
                        -> uriBuilder.path(useChatModel.getUri())
                        .queryParam("access_token", token)
                        .build())
                .body(Mono.just(request), YiYanChatCompletionRequest.class)
                .retrieve()
                .bodyToFlux(YiYanChatCompletionResponse.class);
    }

}
