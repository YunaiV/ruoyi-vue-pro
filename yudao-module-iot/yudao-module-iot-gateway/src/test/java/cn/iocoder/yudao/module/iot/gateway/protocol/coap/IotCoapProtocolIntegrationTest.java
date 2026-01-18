package cn.iocoder.yudao.module.iot.gateway.protocol.coap;

import cn.iocoder.yudao.module.iot.gateway.protocol.coap.router.IotCoapUpstreamHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.junit.jupiter.api.*;

/**
 * IoT 网关 CoAP 协议集成测试（手动测试）
 *
 * 使用步骤：
 * 1. 启动 CoAP 网关服务（端口 5683）
 * 2. 运行 testAuth() 获取 token
 * 3. 将 token 粘贴到 TOKEN 常量
 * 4. 运行 testPropertyPost() 或 testEventPost()
 *
 * @author 芋道源码
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IotCoapProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 5683;

    // 设备信息（根据实际情况修改）
    private static final String PRODUCT_KEY = "testProductKey";
    private static final String DEVICE_NAME = "testDeviceName";
    private static final String CLIENT_ID = PRODUCT_KEY + "." + DEVICE_NAME;
    private static final String USERNAME = DEVICE_NAME + "&" + PRODUCT_KEY;
    private static final String PASSWORD = "testPassword123";

    // TODO: 运行 testAuth() 后，将返回的 token 粘贴到这里
    private static final String TOKEN = "粘贴你的token到这里";

    // ========== 1. 认证测试 ==========

    @Test
    @Order(1)
    @DisplayName("1. 认证 - 获取 Token")
    void testAuth() throws Exception {
        String uri = String.format("coap://%s:%d/auth", SERVER_HOST, SERVER_PORT);

        String payload = String.format("""
                {
                    "clientId": "%s",
                    "username": "%s",
                    "password": "%s"
                }
                """, CLIENT_ID, USERNAME, PASSWORD);

        CoapClient client = new CoapClient(uri);
        try {
            log.info("[testAuth][请求 URI: {}]", uri);
            log.info("[testAuth][请求体: {}]", payload);

            CoapResponse response = client.post(payload, MediaTypeRegistry.APPLICATION_JSON);

            log.info("[testAuth][响应码: {}]", response.getCode());
            log.info("[testAuth][响应体: {}]", response.getResponseText());
            log.info("[testAuth][请将返回的 token 复制到 TOKEN 常量中]");
        } finally {
            client.shutdown();
        }
    }

    // ========== 2. 属性上报测试 ==========

    @Test
    @Order(2)
    @DisplayName("2. 属性上报")
    void testPropertyPost() throws Exception {
        String uri = String.format("coap://%s:%d/topic/sys/%s/%s/thing/property/post",
                SERVER_HOST, SERVER_PORT, PRODUCT_KEY, DEVICE_NAME);

        String payload = """
                {
                    "id": "123",
                    "method": "thing.property.post",
                    "params": {
                        "temperature": 25.5,
                        "humidity": 60
                    }
                }
                """;

        CoapClient client = new CoapClient(uri);
        try {
            // 构造带自定义 Option 的请求
            Request request = Request.newPost();
            request.setURI(uri);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            // 添加自定义 Token Option (2088)
            request.getOptions().addOption(new Option(IotCoapUpstreamHandler.OPTION_TOKEN, TOKEN));

            log.info("[testPropertyPost][请求 URI: {}]", uri);
            log.info("[testPropertyPost][Token: {}]", TOKEN);
            log.info("[testPropertyPost][请求体: {}]", payload);

            CoapResponse response = client.advanced(request);

            log.info("[testPropertyPost][响应码: {}]", response.getCode());
            log.info("[testPropertyPost][响应体: {}]", response.getResponseText());
        } finally {
            client.shutdown();
        }
    }

    // ========== 3. 事件上报测试 ==========

    @Test
    @Order(3)
    @DisplayName("3. 事件上报")
    void testEventPost() throws Exception {
        String uri = String.format("coap://%s:%d/topic/sys/%s/%s/thing/event/alarm/post",
                SERVER_HOST, SERVER_PORT, PRODUCT_KEY, DEVICE_NAME);

        String payload = """
                {
                    "id": "456",
                    "method": "thing.event.alarm.post",
                    "params": {
                        "alarmType": "temperature_high",
                        "level": "warning",
                        "value": 85.2
                    }
                }
                """;

        CoapClient client = new CoapClient(uri);
        try {
            // 构造带自定义 Option 的请求
            Request request = Request.newPost();
            request.setURI(uri);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            // 添加自定义 Token Option (2088)
            request.getOptions().addOption(new Option(IotCoapUpstreamHandler.OPTION_TOKEN, TOKEN));

            log.info("[testEventPost][请求 URI: {}]", uri);
            log.info("[testEventPost][Token: {}]", TOKEN);
            log.info("[testEventPost][请求体: {}]", payload);

            CoapResponse response = client.advanced(request);

            log.info("[testEventPost][响应码: {}]", response.getCode());
            log.info("[testEventPost][响应体: {}]", response.getResponseText());
        } finally {
            client.shutdown();
        }
    }

}
