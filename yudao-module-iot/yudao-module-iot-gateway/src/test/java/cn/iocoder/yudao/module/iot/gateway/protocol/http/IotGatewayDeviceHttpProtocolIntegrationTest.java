package cn.iocoder.yudao.module.iot.gateway.protocol.http;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPackPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoAddReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoDeleteReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoGetReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * IoT 网关设备 HTTP 协议集成测试（手动测试）
 *
 * <p>测试场景：网关设备（IotProductDeviceTypeEnum 的 GATEWAY 类型）通过 HTTP 协议管理子设备拓扑关系
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（HTTP 端口 8092）</li>
 *     <li>运行 {@link #testAuth()} 获取网关设备 token，将返回的 token 粘贴到 {@link #GATEWAY_TOKEN} 常量</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testTopoAdd()} - 添加子设备拓扑关系</li>
 *             <li>{@link #testTopoDelete()} - 删除子设备拓扑关系</li>
 *             <li>{@link #testTopoGet()} - 获取子设备拓扑关系</li>
 *             <li>{@link #testSubDeviceRegister()} - 子设备动态注册</li>
 *             <li>{@link #testPropertyPackPost()} - 批量上报属性（网关 + 子设备）</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
@SuppressWarnings("HttpUrlsUsage")
public class IotGatewayDeviceHttpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8092;

    // ===================== 网关设备信息（根据实际情况修改，从 iot_device 表查询网关设备） =====================

    private static final String GATEWAY_PRODUCT_KEY = "m6XcS1ZJ3TW8eC0v";
    private static final String GATEWAY_DEVICE_NAME = "sub-ddd";
    private static final String GATEWAY_DEVICE_SECRET = "b3d62c70f8a4495487ed1d35d61ac2b3";

    /**
     * 网关设备 Token：从 {@link #testAuth()} 方法获取后，粘贴到这里
     */
    private static final String GATEWAY_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9kdWN0S2V5IjoibTZYY1MxWkozVFc4ZUMwdiIsImV4cCI6MTc2OTg2NjY3OCwiZGV2aWNlTmFtZSI6InN1Yi1kZGQifQ.nCLSAfHEjXLtTDRXARjOoFqpuo5WfArjFWweUAzrjKU";

    // ===================== 子设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================

    private static final String SUB_DEVICE_PRODUCT_KEY = "jAufEMTF1W6wnPhn";
    private static final String SUB_DEVICE_NAME = "chazuo-it";
    private static final String SUB_DEVICE_SECRET = "d46ef9b28ab14238b9c00a3a668032af";

    // ===================== 认证测试 =====================

    /**
     * 网关设备认证测试：获取网关设备 Token
     */
    @Test
    public void testAuth() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/auth", SERVER_HOST, SERVER_PORT);
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
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
        log.info("[testAuth][请将返回的 token 复制到 GATEWAY_TOKEN 常量中]");
    }

    // ===================== 拓扑管理测试 =====================

    /**
     * 添加子设备拓扑关系测试
     * <p>
     * 网关设备向平台上报需要绑定的子设备信息
     */
    @Test
    public void testTopoAdd() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/topic/sys/%s/%s/thing/topo/add",
                SERVER_HOST, SERVER_PORT, GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        // 1.2 构建子设备认证信息
        IotDeviceAuthReqDTO subAuthInfo = IotDeviceAuthUtils.getAuthInfo(
                SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME, SUB_DEVICE_SECRET);
        IotDeviceAuthReqDTO subDeviceAuth = new IotDeviceAuthReqDTO()
                .setClientId(subAuthInfo.getClientId())
                .setUsername(subAuthInfo.getUsername())
                .setPassword(subAuthInfo.getPassword());
        // 1.3 构建请求参数
        IotDeviceTopoAddReqDTO params = new IotDeviceTopoAddReqDTO();
        params.setSubDevices(Collections.singletonList(subDeviceAuth));
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.TOPO_ADD.getMethod())
                .put("version", "1.0")
                .put("params", params)
                .build());
        // 1.4 输出请求
        log.info("[testTopoAdd][请求 URL: {}]", url);
        log.info("[testTopoAdd][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Authorization", GATEWAY_TOKEN)
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testTopoAdd][响应体: {}]", httpResponse.body());
        }
    }

    /**
     * 删除子设备拓扑关系测试
     * <p>
     * 网关设备向平台上报需要解绑的子设备信息
     */
    @Test
    public void testTopoDelete() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/topic/sys/%s/%s/thing/topo/delete",
                SERVER_HOST, SERVER_PORT, GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        // 1.2 构建请求参数
        IotDeviceTopoDeleteReqDTO params = new IotDeviceTopoDeleteReqDTO();
        params.setSubDevices(Collections.singletonList(
                new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME)));
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod())
                .put("version", "1.0")
                .put("params", params)
                .build());
        // 1.3 输出请求
        log.info("[testTopoDelete][请求 URL: {}]", url);
        log.info("[testTopoDelete][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Authorization", GATEWAY_TOKEN)
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testTopoDelete][响应体: {}]", httpResponse.body());
        }
    }

    /**
     * 获取子设备拓扑关系测试
     * <p>
     * 网关设备向平台查询已绑定的子设备列表
     */
    @Test
    public void testTopoGet() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/topic/sys/%s/%s/thing/topo/get",
                SERVER_HOST, SERVER_PORT, GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        // 1.2 构建请求参数（目前为空，预留扩展）
        IotDeviceTopoGetReqDTO params = new IotDeviceTopoGetReqDTO();
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.TOPO_GET.getMethod())
                .put("version", "1.0")
                .put("params", params)
                .build());
        // 1.3 输出请求
        log.info("[testTopoGet][请求 URL: {}]", url);
        log.info("[testTopoGet][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Authorization", GATEWAY_TOKEN)
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testTopoGet][响应体: {}]", httpResponse.body());
        }
    }

    // ===================== 子设备注册测试 =====================

    // TODO @芋艿：待测试

    /**
     * 子设备动态注册测试
     * <p>
     * 网关设备代理子设备进行动态注册，平台返回子设备的 deviceSecret
     * <p>
     * 注意：此接口需要网关 Token 认证
     */
    @Test
    public void testSubDeviceRegister() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/auth/register/sub-device/%s/%s",
                SERVER_HOST, SERVER_PORT, GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        // 1.2 构建请求参数
        IotSubDeviceRegisterReqDTO subDevice = new IotSubDeviceRegisterReqDTO();
        subDevice.setProductKey(SUB_DEVICE_PRODUCT_KEY);
        subDevice.setDeviceName("mougezishebei");
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod())
                .put("version", "1.0")
                .put("params", Collections.singletonList(subDevice))
                .build());
        // 1.3 输出请求
        log.info("[testSubDeviceRegister][请求 URL: {}]", url);
        log.info("[testSubDeviceRegister][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Authorization", GATEWAY_TOKEN)
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testSubDeviceRegister][响应体: {}]", httpResponse.body());
        }
    }

    // ===================== 批量上报测试 =====================

    /**
     * 批量上报属性测试（网关 + 子设备）
     * <p>
     * 网关设备批量上报自身属性、事件，以及子设备的属性、事件
     */
    @Test
    public void testPropertyPackPost() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/topic/sys/%s/%s/thing/event/property/pack/post",
                SERVER_HOST, SERVER_PORT, GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        // 1.2 构建【网关设备】自身属性
        Map<String, Object> gatewayProperties = MapUtil.<String, Object>builder()
                .put("temperature", 25.5)
                .build();
        // 1.3 构建【网关设备】自身事件
        IotDevicePropertyPackPostReqDTO.EventValue gatewayEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
        gatewayEvent.setValue(MapUtil.builder().put("message", "gateway started").build());
        gatewayEvent.setTime(System.currentTimeMillis());
        Map<String, IotDevicePropertyPackPostReqDTO.EventValue> gatewayEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                .put("statusReport", gatewayEvent)
                .build();
        // 1.4 构建【网关子设备】属性
        Map<String, Object> subDeviceProperties = MapUtil.<String, Object>builder()
                .put("power", 100)
                .build();
        // 1.5 构建【网关子设备】事件
        IotDevicePropertyPackPostReqDTO.EventValue subDeviceEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
        subDeviceEvent.setValue(MapUtil.builder().put("errorCode", 0).build());
        subDeviceEvent.setTime(System.currentTimeMillis());
        Map<String, IotDevicePropertyPackPostReqDTO.EventValue> subDeviceEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                .put("healthCheck", subDeviceEvent)
                .build();
        // 1.6 构建子设备数据
        IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData = new IotDevicePropertyPackPostReqDTO.SubDeviceData();
        subDeviceData.setIdentity(new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME));
        subDeviceData.setProperties(subDeviceProperties);
        subDeviceData.setEvents(subDeviceEvents);
        // 1.7 构建请求参数
        IotDevicePropertyPackPostReqDTO params = new IotDevicePropertyPackPostReqDTO();
        params.setProperties(gatewayProperties);
        params.setEvents(gatewayEvents);
        params.setSubDevices(ListUtil.of(subDeviceData));
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod())
                .put("version", "1.0")
                .put("params", params)
                .build());
        // 1.8 输出请求
        log.info("[testPropertyPackPost][请求 URL: {}]", url);
        log.info("[testPropertyPackPost][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Authorization", GATEWAY_TOKEN)
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testPropertyPackPost][响应体: {}]", httpResponse.body());
        }
    }

}
