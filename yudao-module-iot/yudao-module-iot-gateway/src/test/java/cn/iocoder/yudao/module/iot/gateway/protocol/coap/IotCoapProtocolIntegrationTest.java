package cn.iocoder.yudao.module.iot.gateway.protocol.coap;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.util.IotCoapUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.config.UdpConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * IoT 网关 CoAP 协议集成测试（手动测试）
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（CoAP 端口 5683）</li>
 *     <li>运行 {@link #testAuth()} 获取 token，将返回的 token 粘贴到 {@link #TOKEN} 常量</li>
 *     <li>运行 {@link #testPropertyPost()} 测试属性上报，或运行 {@link #testEventPost()} 测试事件上报</li>
 * </ol>
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 5683;

    // 设备信息（根据实际情况修改 PRODUCT_KEY、DEVICE_NAME、PASSWORD）
    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String PASSWORD = "509e2b08f7598eb139d276388c600435913ba4c94cd0d50aebc5c0d1855bcb75";

    private static final String CLIENT_ID = PRODUCT_KEY + "." + DEVICE_NAME;
    private static final String USERNAME = DEVICE_NAME + "&" + PRODUCT_KEY;

    /**
     * 设备 Token：从 {@link #testAuth()} 方法获取后，粘贴到这里
     */
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9kdWN0S2V5IjoiNGF5bVpnT1RPT0NyREtSVCIsImV4cCI6MTc2OTMwNTA1NSwiZGV2aWNlTmFtZSI6InNtYWxsIn0.mf3MEATCn5bp6cXgULunZjs8d00RGUxj96JEz0hMS7k";

    @BeforeAll
    public static void initCaliforniumConfig() {
        // 注册 Californium 配置定义
        CoapConfig.register();
        UdpConfig.register();
        // 创建默认配置
        Configuration.setStandard(Configuration.createStandardWithoutFile());
    }

    /**
     * 认证测试：获取设备 Token
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testAuth() throws Exception {
        String uri = String.format("coap://%s:%d/auth", SERVER_HOST, SERVER_PORT);
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("clientId", CLIENT_ID)
                .put("username", USERNAME)
                .put("password", PASSWORD)
                .build());

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

    /**
     * 属性上报测试
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testPropertyPost() throws Exception {
        String uri = String.format("coap://%s:%d/topic/sys/%s/%s/thing/property/post",
                SERVER_HOST, SERVER_PORT, PRODUCT_KEY, DEVICE_NAME);
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                .put("version", "1.0")
                .put("params", MapUtil.builder()
                        .put("width", 1)
                        .put("height", "2")
                        .build())
                .build());

        CoapClient client = new CoapClient(uri);
        try {
            Request request = Request.newPost();
            request.setURI(uri);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            request.getOptions().addOption(new Option(IotCoapUtils.OPTION_TOKEN, TOKEN));

            log.info("[testPropertyPost][请求 URI: {}]", uri);
            log.info("[testPropertyPost][请求体: {}]", payload);

            CoapResponse response = client.advanced(request);

            log.info("[testPropertyPost][响应码: {}]", response.getCode());
            log.info("[testPropertyPost][响应体: {}]", response.getResponseText());
        } finally {
            client.shutdown();
        }
    }

    /**
     * 事件上报测试
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testEventPost() throws Exception {
        String uri = String.format("coap://%s:%d/topic/sys/%s/%s/thing/event/post",
                SERVER_HOST, SERVER_PORT, PRODUCT_KEY, DEVICE_NAME);
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.EVENT_POST.getMethod())
                .put("version", "1.0")
                .put("identifier", "eat")
                .put("params", MapUtil.builder()
                        .put("width", 1)
                        .put("height", "2")
                        .put("oneThree", "3")
                        .build())
                .build());

        CoapClient client = new CoapClient(uri);
        try {
            Request request = Request.newPost();
            request.setURI(uri);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            request.getOptions().addOption(new Option(IotCoapUtils.OPTION_TOKEN, TOKEN));

            log.info("[testEventPost][请求 URI: {}]", uri);
            log.info("[testEventPost][请求体: {}]", payload);

            CoapResponse response = client.advanced(request);

            log.info("[testEventPost][响应码: {}]", response.getCode());
            log.info("[testEventPost][响应体: {}]", response.getResponseText());
        } finally {
            client.shutdown();
        }
    }

}
