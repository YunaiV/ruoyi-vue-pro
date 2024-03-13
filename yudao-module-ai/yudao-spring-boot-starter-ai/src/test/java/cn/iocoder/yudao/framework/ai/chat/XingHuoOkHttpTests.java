package cn.iocoder.yudao.framework.ai.chat;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatClient;
import cn.iocoder.yudao.framework.ai.chatxinghuo.api.XingHuoChatCompletion;
import cn.iocoder.yudao.framework.ai.chatxinghuo.api.XingHuoChatCompletionRequest;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 讯飞星火 tests
 * <p>
 * author: fansili
 * time: 2024/3/11 11:00
 */
public class XingHuoOkHttpTests {

    private static final String HOST_URL = "http://spark-api.xf-yun.com/v3.5/chat";
    private static final String API_KEY = "cb6415c19d6162cda07b47316fcb0416";
    private static final String API_SECRET = "Y2JiYTIxZjA3MDMxMjNjZjQzYzVmNzdh";

    private XingHuoChatClient xingHuoChatClient;

    public static void main(String[] args) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException {
        String authUrl = getAuthorizationUrl("spark-api.xf-yun.com", "/v3.5/chat");
        System.err.println(authUrl);

        XingHuoChatCompletionRequest.Header header = new XingHuoChatCompletionRequest.Header().setApp_id("13c8cca6");
        XingHuoChatCompletionRequest.Parameter parameter
                = new XingHuoChatCompletionRequest.Parameter()
                .setChat(new XingHuoChatCompletionRequest.Parameter.Chat().setDomain("generalv3.5"));


        XingHuoChatCompletionRequest.Payload.Message.Text text = new XingHuoChatCompletionRequest.Payload.Message.Text();
        text.setRole(XingHuoChatCompletionRequest.Payload.Message.Text.Role.USER.getName());
        text.setContent("世界上最好的开发语言是什么？");
        XingHuoChatCompletionRequest.Payload payload = new XingHuoChatCompletionRequest.Payload()
                .setMessage(new XingHuoChatCompletionRequest.Payload.Message().setText(List.of(text)));
        XingHuoChatCompletionRequest request = new XingHuoChatCompletionRequest()
                .setHeader(header)
                .setParameter(parameter)
                .setPayload(payload);

        System.err.println(JSONUtil.toJsonPrettyStr(request));

        OkHttpClient client = new OkHttpClient();
        Request request2 = new Request.Builder()
                .url(authUrl) // 替换为你的 wss URL
                .build();

        WebSocketListener webSocketListener = new WebSocketListener() {

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                boolean send = webSocket.send(JSONUtil.toJsonStr(request));
                System.err.println("发送 -> " + send);
                System.err.println("链接成功!");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
//                System.err.println(text);
                XingHuoChatCompletion response = JSONUtil.toBean(text, XingHuoChatCompletion.class);
                for (XingHuoChatCompletion.Text text1 : response.getPayload().getChoices().getText()) {
                    System.err.print(text1.getContent());
                }
            }
        };

        WebSocket webSocket = client.newWebSocket(request2, webSocketListener);
//        webSocket.send(JSONUtil.toJsonStr(request));


        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
        // 阻止退出
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }


    /**
     * 获取验证请求url
     *
     * @return
     */
    public static String getAuthorizationUrl(String host, String path) throws NoSuchAlgorithmException, InvalidKeyException {
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
        SecretKeySpec sp = new SecretKeySpec(API_SECRET.getBytes(charset), "hmacsha256");
        mac.init(sp);
        byte[] basebefore = mac.doFinal(builder.toString().getBytes(charset));
        String signature = Base64.getEncoder().encodeToString(basebefore);
        //获得 authorization_origin
        String authorization_origin = String.format("api_key=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"", API_KEY, "hmac-sha256", "host date request-line", signature);
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

}
