package cn.iocoder.yudao.framework.ai.chatqianwen;

import com.aliyun.broadscope.bailian.sdk.AccessTokenClient;
import com.aliyun.broadscope.bailian.sdk.ApplicationClient;
import com.aliyun.broadscope.bailian.sdk.models.ChatRequestMessage;
import com.aliyun.broadscope.bailian.sdk.models.CompletionsRequest;
import com.aliyun.broadscope.bailian.sdk.models.CompletionsResponse;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 阿里 通义千问
 *
 * https://www.aliyun.com/search?k=%E9%80%9A%E4%B9%89%E5%A4%A7%E6%A8%A1%E5%9E%8B&scene=all
 *
 * author: fansili
 * time: 2024/3/13 21:09
 */
@Getter
public class QianWenApi {

    /**
     * accessKeyId、accessKeySecret、agentKey、appId 获取方式如下链接
     * https://help.aliyun.com/document_detail/2587494.html?spm=a2c4g.2587492.0.0.53f33c566sXskp
     */
    private String accessKeyId;
    private String accessKeySecret;
    private String agentKey;
    private String appId;
    private String endpoint = "bailian.cn-beijing.aliyuncs.com";
    private String token;
    private ApplicationClient client;

    public QianWenApi(String accessKeyId, String accessKeySecret, String agentKey, String appId, String endpoint) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.agentKey = agentKey;
        this.appId = appId;

        if (endpoint != null) {
            this.endpoint = endpoint;
        }

        // 获取token
        AccessTokenClient accessTokenClient = new AccessTokenClient(accessKeyId, accessKeySecret, agentKey);
        token = accessTokenClient.getToken();
        // 构建client
        client = ApplicationClient.builder()
                .token(token)
                .build();
    }

    public ResponseEntity<CompletionsResponse> chatCompletionEntity(ChatRequestMessage message) {
        // 创建request
        CompletionsRequest request = new CompletionsRequest()
                // 设置 appid
                .setAppId(appId)
                .setMessages(List.of(message))
                // 返回choice message结果
                .setParameters(new CompletionsRequest.Parameter().setResultFormat("message"));
        //
        CompletionsResponse response = client.completions(request);
        int httpCode = 200;
        if (!response.isSuccess()) {
            System.out.printf("failed to create completion, requestId: %s, code: %s, message: %s\n",
                    response.getRequestId(), response.getCode(), response.getMessage());
            httpCode = 500;
        }
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(httpCode));
    }

    public Flux<CompletionsResponse> chatCompletionStream(ChatRequestMessage message) {
        return client.streamCompletions(
                new CompletionsRequest()
                        // 设置 appid
                        .setAppId(appId)
                        .setMessages(List.of(message))
                        //开启增量输出模式，后面输出不会包含已经输出的内容
                        .setParameters(new CompletionsRequest.Parameter().setIncrementalOutput(true))
        );
    }
}
