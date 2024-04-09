package cn.iocoder.yudao.framework.ai.chatqianwen.api;

import com.aliyun.broadscope.bailian.sdk.AccessTokenClient;
import com.aliyun.broadscope.bailian.sdk.ApplicationClient;
import com.aliyun.broadscope.bailian.sdk.models.CompletionsRequest;
import com.aliyun.broadscope.bailian.sdk.models.CompletionsResponse;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

// TODO done @fansili：是不是挪到 api 包里？按照 spring ai 的结构；根目录只放 client 和 options
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

    public QianWenApi(String accessKeyId, String accessKeySecret, String agentKey, String endpoint) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.agentKey = agentKey;

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

    public ResponseEntity<CompletionsResponse> chatCompletionEntity(CompletionsRequest request) {
        // 发送请求
        CompletionsResponse response = client.completions(request);
        // 阿里云的这个 http code 随便设置，外面判断是否成功用的 CompletionsResponse.isSuccess
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
    }

    public Flux<CompletionsResponse> chatCompletionStream(CompletionsRequest request) {
        return client.streamCompletions(request);
    }
}
