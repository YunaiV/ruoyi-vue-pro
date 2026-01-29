package cn.iocoder.yudao.module.iot.gateway.protocol.coap;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * IoT 网关设备 CoAP 协议集成测试（手动测试）
 *
 * <p>测试场景：网关设备（IotProductDeviceTypeEnum 的 GATEWAY 类型）通过 CoAP 协议管理子设备拓扑关系
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（CoAP 端口 5683）</li>
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
public class IotGatewayDeviceCoapProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 5683;

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

    @BeforeAll
    public static void initCaliforniumConfig() {
        // 注册 Californium 配置定义
        CoapConfig.register();
        UdpConfig.register();
        // 创建默认配置
        Configuration.setStandard(Configuration.createStandardWithoutFile());
    }

    // ===================== 认证测试 =====================

    /**
     * 网关设备认证测试：获取网关设备 Token
     */
    @Test
    public void testAuth() throws Exception {
        // 1.1 构建请求
        String uri = String.format("coap://%s:%d/auth", SERVER_HOST, SERVER_PORT);
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        String payload = JsonUtils.toJsonString(authReqDTO);
        // 1.2 输出请求
        log.info("[testAuth][请求 URI: {}]", uri);
        log.info("[testAuth][请求体: {}]", payload);

        // 2.1 发送请求
        CoapClient client = new CoapClient(uri);
        try {
            CoapResponse response = client.post(payload, MediaTypeRegistry.APPLICATION_JSON);
            // 2.2 输出结果
            log.info("[testAuth][响应码: {}]", response.getCode());
            log.info("[testAuth][响应体: {}]", response.getResponseText());
            log.info("[testAuth][请将返回的 token 复制到 GATEWAY_TOKEN 常量中]");
        } finally {
            client.shutdown();
        }
    }

    // ===================== 拓扑管理测试 =====================

    /**
     * 添加子设备拓扑关系测试
     * <p>
     * 网关设备向平台上报需要绑定的子设备信息
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testTopoAdd() throws Exception {
        // 1.1 构建请求
        String uri = String.format("coap://%s:%d/topic/sys/%s/%s/thing/topo/add",
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
        log.info("[testTopoAdd][请求 URI: {}]", uri);
        log.info("[testTopoAdd][请求体: {}]", payload);

        // 2.1 发送请求
        CoapClient client = new CoapClient(uri);
        try {
            Request request = Request.newPost();
            request.setURI(uri);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            request.getOptions().addOption(new Option(IotCoapUtils.OPTION_TOKEN, GATEWAY_TOKEN));

            CoapResponse response = client.advanced(request);
            // 2.2 输出结果
            log.info("[testTopoAdd][响应码: {}]", response.getCode());
            log.info("[testTopoAdd][响应体: {}]", response.getResponseText());
        } finally {
            client.shutdown();
        }
    }

    /**
     * 删除子设备拓扑关系测试
     * <p>
     * 网关设备向平台上报需要解绑的子设备信息
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testTopoDelete() throws Exception {
        // 1.1 构建请求
        String uri = String.format("coap://%s:%d/topic/sys/%s/%s/thing/topo/delete",
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
        log.info("[testTopoDelete][请求 URI: {}]", uri);
        log.info("[testTopoDelete][请求体: {}]", payload);

        // 2.1 发送请求
        CoapClient client = new CoapClient(uri);
        try {
            Request request = Request.newPost();
            request.setURI(uri);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            request.getOptions().addOption(new Option(IotCoapUtils.OPTION_TOKEN, GATEWAY_TOKEN));

            CoapResponse response = client.advanced(request);
            // 2.2 输出结果
            log.info("[testTopoDelete][响应码: {}]", response.getCode());
            log.info("[testTopoDelete][响应体: {}]", response.getResponseText());
        } finally {
            client.shutdown();
        }
    }

    /**
     * 获取子设备拓扑关系测试
     * <p>
     * 网关设备向平台查询已绑定的子设备列表
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testTopoGet() throws Exception {
        // 1.1 构建请求
        String uri = String.format("coap://%s:%d/topic/sys/%s/%s/thing/topo/get",
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
        log.info("[testTopoGet][请求 URI: {}]", uri);
        log.info("[testTopoGet][请求体: {}]", payload);

        // 2.1 发送请求
        CoapClient client = new CoapClient(uri);
        try {
            Request request = Request.newPost();
            request.setURI(uri);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            request.getOptions().addOption(new Option(IotCoapUtils.OPTION_TOKEN, GATEWAY_TOKEN));

            CoapResponse response = client.advanced(request);
            // 2.2 输出结果
            log.info("[testTopoGet][响应码: {}]", response.getCode());
            log.info("[testTopoGet][响应体: {}]", response.getResponseText());
        } finally {
            client.shutdown();
        }
    }

    // ===================== 子设备注册测试 =====================

    /**
     * 子设备动态注册测试
     * <p>
     * 网关设备代理子设备进行动态注册，平台返回子设备的 deviceSecret
     * <p>
     * 注意：此接口需要网关 Token 认证
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testSubDeviceRegister() throws Exception {
        // 1.1 构建请求
        String uri = String.format("coap://%s:%d/auth/register/sub-device/%s/%s",
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
        log.info("[testSubDeviceRegister][请求 URI: {}]", uri);
        log.info("[testSubDeviceRegister][请求体: {}]", payload);

        // 2.1 发送请求
        CoapClient client = new CoapClient(uri);
        try {
            Request request = Request.newPost();
            request.setURI(uri);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            request.getOptions().addOption(new Option(IotCoapUtils.OPTION_TOKEN, GATEWAY_TOKEN));

            CoapResponse response = client.advanced(request);
            // 2.2 输出结果
            log.info("[testSubDeviceRegister][响应码: {}]", response.getCode());
            log.info("[testSubDeviceRegister][响应体: {}]", response.getResponseText());
        } finally {
            client.shutdown();
        }
    }

    // ===================== 批量上报测试 =====================

    /**
     * 批量上报属性测试（网关 + 子设备）
     * <p>
     * 网关设备批量上报自身属性、事件，以及子设备的属性、事件
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testPropertyPackPost() throws Exception {
        // 1.1 构建请求
        String uri = String.format("coap://%s:%d/topic/sys/%s/%s/thing/event/property/pack/post",
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
        params.setSubDevices(List.of(subDeviceData));
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod())
                .put("version", "1.0")
                .put("params", params)
                .build());
        // 1.8 输出请求
        log.info("[testPropertyPackPost][请求 URI: {}]", uri);
        log.info("[testPropertyPackPost][请求体: {}]", payload);

        // 2.1 发送请求
        CoapClient client = new CoapClient(uri);
        try {
            Request request = Request.newPost();
            request.setURI(uri);
            request.setPayload(payload);
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            request.getOptions().addOption(new Option(IotCoapUtils.OPTION_TOKEN, GATEWAY_TOKEN));

            CoapResponse response = client.advanced(request);
            // 2.2 输出结果
            log.info("[testPropertyPackPost][响应码: {}]", response.getCode());
            log.info("[testPropertyPackPost][响应体: {}]", response.getResponseText());
        } finally {
            client.shutdown();
        }
    }

}
