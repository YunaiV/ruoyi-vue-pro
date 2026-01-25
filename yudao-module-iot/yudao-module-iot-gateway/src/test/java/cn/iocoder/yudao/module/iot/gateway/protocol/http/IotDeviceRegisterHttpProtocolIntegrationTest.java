package cn.iocoder.yudao.module.iot.gateway.protocol.http;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collections;

// TODO @AI：合并到 IotDirectDeviceHttpProtocolIntegrationTest 里呀，没必要拆开；只搞一个直连设备的注册就好了；
/**
 * IoT 设备动态注册 HTTP 协议集成测试（手动测试）
 *
 * <p>测试场景：一型一密（One Type One Secret）动态注册机制
 *
 * <p><b>前置条件：</b>
 * <ol>
 *     <li>产品已开启动态注册（registerEnabled = true）</li>
 *     <li>设备已预先创建（预注册模式）</li>
 *     <li>设备 deviceSecret 为空（未激活状态）</li>
 * </ol>
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（HTTP 端口 8092）</li>
 *     <li>运行 {@link #testDeviceRegister()} 测试直连设备/网关动态注册</li>
 *     <li>运行 {@link #testSubDeviceRegister()} 测试子设备动态注册（需要先获取网关 Token）</li>
 * </ol>
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
 */
@Slf4j
@SuppressWarnings("HttpUrlsUsage")
public class IotDeviceRegisterHttpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8092;

    // ===================== 直连设备/网关动态注册配置（根据实际情况修改） =====================
    /**
     * 产品 Key（需要开启动态注册）
     */
    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    /**
     * 产品密钥（从 iot_product 表的 product_secret 字段获取）
     */
    private static final String PRODUCT_SECRET = "your_product_secret";
    /**
     * 设备名称（需要预先创建，deviceSecret 为空）
     */
    private static final String DEVICE_NAME = "test-register-device";

    // ===================== 网关设备信息（用于子设备动态注册） =====================
    private static final String GATEWAY_PRODUCT_KEY = "m6XcS1ZJ3TW8eC0v";
    private static final String GATEWAY_DEVICE_NAME = "sub-ddd";
    private static final String GATEWAY_DEVICE_SECRET = "b3d62c70f8a4495487ed1d35d61ac2b3";

    /**
     * 网关设备 Token：从网关认证获取后，粘贴到这里
     */
    private static final String GATEWAY_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9kdWN0S2V5IjoibTZYY1MxWkozVFc4ZUMwdiIsImV4cCI6MTc2OTg2NjY3OCwiZGV2aWNlTmFtZSI6InN1Yi1kZGQifQ.nCLSAfHEjXLtTDRXARjOoFqpuo5WfArjFWweUAzrjKU";

    // ===================== 子设备信息（用于子设备动态注册） =====================
    private static final String SUB_DEVICE_PRODUCT_KEY = "jAufEMTF1W6wnPhn";
    private static final String SUB_DEVICE_NAME = "test-sub-register-device";

    // ===================== 直连设备/网关动态注册测试 =====================

    /**
     * 直连设备/网关动态注册测试
     * <p>
     * 使用产品密钥（productSecret）进行签名验证，成功后返回设备密钥（deviceSecret）
     * <p>
     * 注意：此接口不需要 Token 认证
     */
    @Test
    public void testDeviceRegister() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/auth/register/device", SERVER_HOST, SERVER_PORT);
        // 1.2 生成签名
        String random = IdUtil.fastSimpleUUID();
        String sign = IotDeviceAuthUtils.buildRegisterSign(PRODUCT_SECRET, PRODUCT_KEY, DEVICE_NAME, random);
        // 1.3 构建请求参数
        IotDeviceRegisterReqDTO reqDTO = new IotDeviceRegisterReqDTO();
        reqDTO.setProductKey(PRODUCT_KEY);
        reqDTO.setDeviceName(DEVICE_NAME);
        reqDTO.setRandom(random);
        reqDTO.setSign(sign);
        String payload = JsonUtils.toJsonString(reqDTO);
        // 1.4 输出请求
        log.info("[testDeviceRegister][请求 URL: {}]", url);
        log.info("[testDeviceRegister][请求体: {}]", payload);

        // 2.1 发送请求
        String response = HttpUtil.post(url, payload);
        // 2.2 输出结果
        log.info("[testDeviceRegister][响应体: {}]", response);
        log.info("[testDeviceRegister][成功后可使用返回的 deviceSecret 进行一机一密认证]");
    }

    /**
     * 测试动态注册后使用 deviceSecret 进行认证
     * <p>
     * 此测试需要先执行 testDeviceRegister 获取 deviceSecret
     */
    @Test
    public void testAuthAfterRegister() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/auth", SERVER_HOST, SERVER_PORT);
        // TODO 将 testDeviceRegister 返回的 deviceSecret 填入此处
        String deviceSecret = "返回的deviceSecret";
        IotDeviceAuthUtils.AuthInfo authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, deviceSecret);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        String payload = JsonUtils.toJsonString(authReqDTO);
        // 1.2 输出请求
        log.info("[testAuthAfterRegister][请求 URL: {}]", url);
        log.info("[testAuthAfterRegister][请求体: {}]", payload);

        // 2.1 发送请求
        String response = HttpUtil.post(url, payload);
        // 2.2 输出结果
        log.info("[testAuthAfterRegister][响应体: {}]", response);
    }

    // ===================== 网关认证测试 =====================

    /**
     * 网关设备认证测试：获取网关设备 Token（用于后续子设备动态注册）
     */
    @Test
    public void testGatewayAuth() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/auth", SERVER_HOST, SERVER_PORT);
        IotDeviceAuthUtils.AuthInfo authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        String payload = JsonUtils.toJsonString(authReqDTO);
        // 1.2 输出请求
        log.info("[testGatewayAuth][请求 URL: {}]", url);
        log.info("[testGatewayAuth][请求体: {}]", payload);

        // 2.1 发送请求
        String response = HttpUtil.post(url, payload);
        // 2.2 输出结果
        log.info("[testGatewayAuth][响应体: {}]", response);
        log.info("[testGatewayAuth][请将返回的 token 复制到 GATEWAY_TOKEN 常量中]");
    }

    // ===================== 子设备动态注册测试 =====================

    /**
     * 子设备动态注册测试
     * <p>
     * 网关设备代理子设备进行动态注册，平台返回子设备的 deviceSecret
     * <p>
     * 注意：此接口需要网关 Token 认证
     */
    @Test
    public void testSubDeviceRegister() {
        // 1.1 构建请求（需要网关认证）
        String url = String.format("http://%s:%d/auth/register/sub-device/%s/%s",
                SERVER_HOST, SERVER_PORT, GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        // 1.2 构建请求参数
        IotSubDeviceRegisterReqDTO subDevice = new IotSubDeviceRegisterReqDTO();
        subDevice.setProductKey(SUB_DEVICE_PRODUCT_KEY);
        subDevice.setDeviceName(SUB_DEVICE_NAME);
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod())
                .put("version", "1.0")
                .put("params", Collections.singletonList(subDevice))
                .build());
        // 1.3 输出请求
        log.info("[testSubDeviceRegister][请求 URL: {}]", url);
        log.info("[testSubDeviceRegister][请求体: {}]", payload);

        // 2.1 发送请求（需要网关 Token）
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Authorization", GATEWAY_TOKEN)
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testSubDeviceRegister][响应体: {}]", httpResponse.body());
            log.info("[testSubDeviceRegister][成功后可使用返回的 deviceSecret 进行子设备认证]");
        }
    }

}
