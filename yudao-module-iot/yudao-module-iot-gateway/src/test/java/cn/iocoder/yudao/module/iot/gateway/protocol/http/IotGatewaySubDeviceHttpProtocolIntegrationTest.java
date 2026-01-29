package cn.iocoder.yudao.module.iot.gateway.protocol.http;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * IoT 网关子设备 HTTP 协议集成测试（手动测试）
 *
 * <p>测试场景：子设备（IotProductDeviceTypeEnum 的 SUB 类型）通过网关设备代理上报数据
 *
 * <p><b>重要说明：子设备无法直接连接平台，所有请求均由网关设备（Gateway）代为转发。</b>
 * <p>网关设备转发子设备请求时，URL 和 Token 都使用子设备自己的信息。
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（HTTP 端口 8092）</li>
 *     <li>确保子设备已通过 {@link IotGatewayDeviceHttpProtocolIntegrationTest#testTopoAdd()} 绑定到网关</li>
 *     <li>运行 {@link #testAuth()} 获取子设备 token，将返回的 token 粘贴到 {@link #TOKEN} 常量</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testPropertyPost()} - 子设备属性上报（由网关代理转发）</li>
 *             <li>{@link #testEventPost()} - 子设备事件上报（由网关代理转发）</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
@SuppressWarnings("HttpUrlsUsage")
public class IotGatewaySubDeviceHttpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8092;

    // ===================== 网关子设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================

    private static final String PRODUCT_KEY = "jAufEMTF1W6wnPhn";
    private static final String DEVICE_NAME = "chazuo-it";
    private static final String DEVICE_SECRET = "d46ef9b28ab14238b9c00a3a668032af";

    /**
     * 网关子设备 Token：从 {@link #testAuth()} 方法获取后，粘贴到这里
     */
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9kdWN0S2V5IjoiakF1ZkVNVEYxVzZ3blBobiIsImV4cCI6MTc2OTg3MTI3NCwiZGV2aWNlTmFtZSI6ImNoYXp1by1pdCJ9.99sAlRalzMU3CqRlGStDzCwWSBJq6u3PJw48JQ3NpzQ";

    // ===================== 认证测试 =====================

    /**
     * 子设备认证测试：获取子设备 Token
     */
    @Test
    public void testAuth() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/auth", SERVER_HOST, SERVER_PORT);
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        String payload = JsonUtils.toJsonString(authReqDTO);
        // 1.2 输出请求
        log.info("[testAuth][请求 URL: {}]", url);
        log.info("[testAuth][请求体: {}]", payload);

        // 2.1 发送请求
        String response = HttpUtil.post(url, payload);
        // 2.2 输出结果
        log.info("[testAuth][响应体: {}]", response);
        log.info("[testAuth][请将返回的 token 复制到 TOKEN 常量中]");
    }

    // ===================== 子设备属性上报测试 =====================

    /**
     * 子设备属性上报测试
     */
    @Test
    public void testPropertyPost() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/topic/sys/%s/%s/thing/property/post",
                SERVER_HOST, SERVER_PORT, PRODUCT_KEY, DEVICE_NAME);
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                .put("version", "1.0")
                .put("params", IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                        .put("power", 100)
                        .put("status", "online")
                        .put("temperature", 36.5)
                        .build())
                )
                .build());
        // 1.2 输出请求
        log.info("[testPropertyPost][子设备属性上报 - 请求实际由 Gateway 代为转发]");
        log.info("[testPropertyPost][请求 URL: {}]", url);
        log.info("[testPropertyPost][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Authorization", TOKEN)
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testPropertyPost][响应体: {}]", httpResponse.body());
        }
    }

    // ===================== 子设备事件上报测试 =====================

    /**
     * 子设备事件上报测试
     */
    @Test
    public void testEventPost() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/topic/sys/%s/%s/thing/event/post",
                SERVER_HOST, SERVER_PORT, PRODUCT_KEY, DEVICE_NAME);
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.EVENT_POST.getMethod())
                .put("version", "1.0")
                .put("params", IotDeviceEventPostReqDTO.of(
                        "alarm",
                        MapUtil.<String, Object>builder()
                                .put("level", "warning")
                                .put("message", "temperature too high")
                                .put("threshold", 40)
                                .put("current", 42)
                                .build(),
                        System.currentTimeMillis())
                )
                .build());
        // 1.2 输出请求
        log.info("[testEventPost][子设备事件上报 - 请求实际由 Gateway 代为转发]");
        log.info("[testEventPost][请求 URL: {}]", url);
        log.info("[testEventPost][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Authorization", TOKEN)
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testEventPost][响应体: {}]", httpResponse.body());
        }
    }

}
