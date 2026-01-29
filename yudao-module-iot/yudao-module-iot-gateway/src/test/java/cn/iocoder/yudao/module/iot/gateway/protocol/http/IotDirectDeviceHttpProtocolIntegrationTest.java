package cn.iocoder.yudao.module.iot.gateway.protocol.http;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * IoT 直连设备 HTTP 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 HTTP 协议直接连接平台
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（HTTP 端口 8092）</li>
 *     <li>运行 {@link #testDeviceRegister()} 测试直连设备动态注册（一型一密）</li>
 *     <li>运行 {@link #testAuth()} 获取设备 token，将返回的 token 粘贴到 {@link #TOKEN} 常量</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testPropertyPost()} - 设备属性上报</li>
 *             <li>{@link #testEventPost()} - 设备事件上报</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
@SuppressWarnings("HttpUrlsUsage")
public class IotDirectDeviceHttpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8092;

    // ===================== 直连设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================

    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

    /**
     * 直连设备 Token：从 {@link #testAuth()} 方法获取后，粘贴到这里
     */
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9kdWN0S2V5IjoiNGF5bVpnT1RPT0NyREtSVCIsImV4cCI6MTc2OTMwNTA1NSwiZGV2aWNlTmFtZSI6InNtYWxsIn0.mf3MEATCn5bp6cXgULunZjs8d00RGUxj96JEz0hMS7k";

    // ===================== 认证测试 =====================

    /**
     * 认证测试：获取设备 Token
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

    // ===================== 直连设备属性上报测试 =====================

    /**
     * 属性上报测试
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
                        .put("width", 1)
                        .put("height", "2")
                        .build())
                )
                .build());
        // 1.2 输出请求
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

    // ===================== 直连设备事件上报测试 =====================

    /**
     * 事件上报测试
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
                        "eat",
                        MapUtil.<String, Object>builder().put("rice", 3).build(),
                        System.currentTimeMillis())
                )
                .build());
        // 1.2 输出请求
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

    // ===================== 动态注册测试 =====================

    /**
     * 直连设备动态注册测试（一型一密）
     * <p>
     * 使用产品密钥（productSecret）验证身份，成功后返回设备密钥（deviceSecret）
     * <p>
     * 注意：此接口不需要 Token 认证
     */
    @Test
    public void testDeviceRegister() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/auth/register/device", SERVER_HOST, SERVER_PORT);
        // 1.2 构建请求参数
        IotDeviceRegisterReqDTO reqDTO = new IotDeviceRegisterReqDTO();
        reqDTO.setProductKey(PRODUCT_KEY);
        reqDTO.setDeviceName("test-" + System.currentTimeMillis());
        reqDTO.setProductSecret("test-product-secret");
        String payload = JsonUtils.toJsonString(reqDTO);
        // 1.3 输出请求
        log.info("[testDeviceRegister][请求 URL: {}]", url);
        log.info("[testDeviceRegister][请求体: {}]", payload);

        // 2.1 发送请求
        String response = HttpUtil.post(url, payload);
        // 2.2 输出结果
        log.info("[testDeviceRegister][响应体: {}]", response);
        log.info("[testDeviceRegister][成功后可使用返回的 deviceSecret 进行一机一密认证]");
    }

}
