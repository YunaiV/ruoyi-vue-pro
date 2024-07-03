package cn.iocoder.yudao.framework.ai.core.model.xinghuo.api;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import lombok.Data;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

// TODO @fan：讯飞使用 spring websocket 接入，还是 okhttp？确认了，未使用的最好删除下，反正 git 也能找回 history
/**
 * 讯飞星火 属性、api
 * <p>
 * 文档地址：https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E
 * <p>
 * author: fansili
 * time: 2024/3/11 10:12
 */
@Data
public class XingHuoApi {

    private static final String DEFAULT_BASE_URL = "wss://spark-api.xf-yun.com";

    private String appId;
    private String appKey;
    private String secretKey;
    private WebClient webClient;
    // 创建 WebSocketClient 实例
    private ReactorNettyWebSocketClient socketClient = new ReactorNettyWebSocketClient();

    public XingHuoApi(String appId, String appKey, String secretKey) {
        this.appId = appId;
        this.appKey = appKey;
        this.secretKey = secretKey;
    }

    public ResponseEntity<XingHuoChatCompletion> chatCompletionEntity(XingHuoChatCompletionRequest request, XingHuoChatModel xingHuoChatModel) {
        String authUrl;
        try {
//            XingHuoChatModel useChatModel;
            authUrl = getAuthorizationUrl("spark-api.xf-yun.com", xingHuoChatModel.getUri());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        // wss 请求的 URI
        URI uri = URI.create(authUrl);
        // 发起 wss 请求并处理响应
        Flux<XingHuoChatCompletion> messageFlux = Flux.create(sink -> {
            socketClient.execute(uri, session ->
                            session.send(Mono.just(session.textMessage(JSONUtil.toJsonStr(request))))
                                    .thenMany(session.receive()
                                            .map(WebSocketMessage -> {
                                                return JSONUtil.toBean(WebSocketMessage.getPayloadAsText(), XingHuoChatCompletion.class);
                                            })
                                            .doOnNext(sink::next) // 将接收到的消息推送到 Flux 中
                                            .doOnError(sink::error) // 处理错误
                                            .doOnTerminate(sink::complete)) // 完成时关闭 sink
                                    .then())
                    .subscribe(); // 订阅以开始会话
        });
        // 阻塞获取所有结果
        List<XingHuoChatCompletion> responseList = messageFlux.collectList().block();
        // 拼接 content
        String responseContent = responseList.stream().map(item -> {
            // 获取 content
            return item.getPayload().getChoices().getText().stream().map(XingHuoChatCompletion.Text::getContent).collect(Collectors.joining());
        }).collect(Collectors.joining());
        // 将多个合并成一个
        XingHuoChatCompletion xingHuoChatCompletion = new XingHuoChatCompletion();
        xingHuoChatCompletion.setPayload(new XingHuoChatCompletion.Payload().setChoices(new XingHuoChatCompletion.Choices().setText(List.of(new XingHuoChatCompletion.Text().setContent(responseContent)))));
        return new ResponseEntity<>(xingHuoChatCompletion, HttpStatusCode.valueOf(200));
    }


    /**
     * 获取验证请求url
     *
     * @return
     */
    public String getAuthorizationUrl(String host, String path) throws NoSuchAlgorithmException, InvalidKeyException {
        // 获取鉴权时间 date
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        // 获取signature_origin字段
        StringBuilder builder = new StringBuilder("host: ").append(host).append("\n").
                append("date: ").append(date).append("\n").
                append("GET ").append(path).append(" HTTP/1.1");

        // 获得signatue
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec sp = new SecretKeySpec(secretKey.getBytes(charset), "hmacsha256");
        mac.init(sp);
        byte[] basebefore = mac.doFinal(builder.toString().getBytes(charset));
        String signature = Base64.getEncoder().encodeToString(basebefore);
        //获得 authorization_origin
        String authorization_origin = String.format("api_key=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"", appKey, "hmac-sha256", "host date request-line", signature);
        //获得authorization
        String authorization = Base64.getEncoder().encodeToString(authorization_origin.getBytes(charset));
        // 获取httpUrl
        Map<String, Object> param = new HashMap<>();
        param.put("authorization", authorization);
        param.put("date", date);
        param.put("host", host);

        String toParams = HttpUtil.toParams(param);
        return "wss://" + host + path + "?" + toParams;
    }

    public Flux<XingHuoChatCompletion> chatCompletionStream(XingHuoChatCompletionRequest request, XingHuoChatModel xingHuoChatModel) {
        String authUrl;
        try {
            authUrl = getAuthorizationUrl("spark-api.xf-yun.com", xingHuoChatModel.getUri());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        // wss 请求的 URI
        URI uri = URI.create(authUrl);
        // 发起 wss 请求并处理响应
        // 创建一个 Flux 来处理接收到的消息
        return Flux.create(sink -> {
            socketClient.execute(uri, session ->
                            session.send(Mono.just(session.textMessage(JSONUtil.toJsonStr(request))))
                                    .thenMany(session.receive()
                                            .map(WebSocketMessage -> JSONUtil.toBean(WebSocketMessage.getPayloadAsText(), XingHuoChatCompletion.class))
                                            .doOnNext(sink::next) // 将接收到的消息推送到 Flux 中
                                            .doOnError(sink::error) // 处理错误
                                            .doOnTerminate(sink::complete)) // 完成时关闭 sink
                                    .then())
                    .subscribe(); // 订阅以开始会话
        });
    }
}
