package cn.iocoder.yudao.framework.ai.midjourney;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * https://blog.csdn.net/qq_38490457/article/details/125250135
 */
public class DiscordWebSocketClient {

    private static final String DISCORD_GATEWAY_URL = "wss://gateway.discord.gg/?v=9&encoding=json";

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, URISyntaxException {
        StandardWebSocketClient client = new StandardWebSocketClient();
        DiscordWebSocketHandler handler = new DiscordWebSocketHandler();


        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Accept-Encoding", "gzip, deflate, br");
        headers.add("Accept-Language", "zh-CN,zh;q=0.9");
        headers.add("Cache-Control", "no-cache");
        headers.add("Pragma", "no-cache");
        headers.add("Sec-Websocket-Extensions", "permessage-deflate; client_max_window_bits");
        headers.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");

        Future<WebSocketSession> futureSession = client.doHandshake(handler, headers, new URI(DISCORD_GATEWAY_URL));
        WebSocketSession session = futureSession.get(); // 这会阻塞直到连接建立

        // 登录过程（你需要替换 TOKEN 为你的 Discord Bot Token）
//        String token = "YOUR_DISCORD_BOT_TOKEN"; // 请替换为你的 Bot Token
//        String identifyPayload = "{\"op\":2,\"d\":{\"token\":\"" + token + "\",\"properties\":{\"$os\":\"java\",\"$browser\":\"spring-websocket\",\"$device\":\"spring-websocket\"},\"compress\":false,\"large_threshold\":256,\"shard\":[0,1]}}";
//        session.sendMessage(new TextMessage(identifyPayload));

        // 发送心跳以保持连接活跃
        Thread heartbeatThread = new Thread(() -> {
            int interval = 0; // 初始心跳间隔，后续从 Discord 服务器获取
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(interval * 1000); // 等待指定的心跳间隔
                    session.sendMessage(new TextMessage("{\"op\":1,\"d\":null}")); // 发送心跳包
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        heartbeatThread.start();

        // 等待用户输入来保持程序运行（仅用于示例）
        System.in.read();

        // 关闭连接和线程
        session.close();
        heartbeatThread.interrupt();
    }

    private static class DiscordWebSocketHandler implements WebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            Object payload = message.getPayload();

            session.sendMessage(new TextMessage(JSON.toJSONString(createAuthData())));
            String  a= "";
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }


        private JSONObject createAuthData() {
            String userAgentStr = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";
            UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
            String token =  "NTY5MDY4NDAxNzEyOTU1Mzky.G4-Fu0.MzD-7ll-ElbXTTgDPHF-WS_UyhMAfbKN3WyyBc";

            JSONObject connectionProperties = new JSONObject()
                    .put("browser", userAgent.getBrowser().getName())
                    .put("browser_user_agent", userAgentStr)
                    .put("browser_version", userAgent.getVersion())
                    .put("client_build_number", 222963)
                    .put("client_event_source", null)
                    .put("device", "")
                    .put("os", userAgent.getOs().getName())
                    .put("referer", "https://www.midjourney.com")
                    .put("referrer_current", "")
                    .put("referring_domain", "www.midjourney.com")
                    .put("referring_domain_current", "")
                    .put("release_channel", "stable")
                    .put("system_locale", "zh-CN");
            JSONObject presence = new JSONObject()
                    .put("activities", "")
                    .put("afk", false)
                    .put("since", 0)
                    .put("status", "online");
            JSONObject clientState = new JSONObject()
                    .put("api_code_version", 0)
                    .put("guild_versions", "")
                    .put("highest_last_message_id", "0")
                    .put("private_channels_version", "0")
                    .put("read_state_version", 0)
                    .put("user_guild_settings_version", -1)
                    .put("user_settings_version", -1);
            return new JSONObject()
                    .put("capabilities", 16381)
                    .put("client_state", clientState)
                    .put("compress", false)
                    .put("presence", presence)
                    .put("properties", connectionProperties)
                    .put("token", token);
        }
    }
}
