package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPackPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoAddReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoDeleteReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoGetReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpBinaryDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpJsonDeviceMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.DatagramSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotDirectDeviceUdpProtocolIntegrationTest.sendAndReceive;

/**
 * IoT 网关设备 UDP 协议集成测试（手动测试）
 *
 * <p>测试场景：网关设备（IotProductDeviceTypeEnum 的 GATEWAY 类型）通过 UDP 协议管理子设备拓扑关系
 *
 * <p>支持两种编解码格式：
 * <ul>
 *     <li>{@link IotTcpJsonDeviceMessageCodec} - JSON 格式</li>
 *     <li>{@link IotTcpBinaryDeviceMessageCodec} - 二进制格式</li>
 * </ul>
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（UDP 端口 8093）</li>
 *     <li>修改 {@link #CODEC} 选择测试的编解码格式</li>
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
 * <p>注意：UDP 协议是无状态的，每次请求需要在 params 中携带 token（与 HTTP 通过 Header 传递不同）
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
public class IotGatewayDeviceUdpProtocolIntegrationTest {

    private static final int TIMEOUT_MS = 5000;

    // ===================== 编解码器选择（修改此处切换 JSON / Binary） =====================

    private static final IotDeviceMessageCodec CODEC = new IotTcpJsonDeviceMessageCodec();
//    private static final IotDeviceMessageCodec CODEC = new IotTcpBinaryDeviceMessageCodec();

    // ===================== 网关设备信息（根据实际情况修改，从 iot_device 表查询网关设备） =====================

    private static final String GATEWAY_PRODUCT_KEY = "m6XcS1ZJ3TW8eC0v";
    private static final String GATEWAY_DEVICE_NAME = "sub-ddd";
    private static final String GATEWAY_DEVICE_SECRET = "b3d62c70f8a4495487ed1d35d61ac2b3";

    /**
     * 网关设备 Token：从 {@link #testAuth()} 方法获取后，粘贴到这里
     */
    private static final String GATEWAY_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9kdWN0S2V5IjoibTZYY1MxWkozVFc4ZUMwdiIsImV4cCI6MTc2OTk1NDcxNSwiZGV2aWNlTmFtZSI6InN1Yi1kZGQifQ.Vg5iateNrpg0FVQI2eJomggxrYXGpwug8wsz9BsVr5w";

    // ===================== 子设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================
    private static final String SUB_DEVICE_PRODUCT_KEY = "jAufEMTF1W6wnPhn";
    private static final String SUB_DEVICE_NAME = "chazuo-it";
    private static final String SUB_DEVICE_SECRET = "d46ef9b28ab14238b9c00a3a668032af";

    // ===================== 认证测试 =====================

    /**
     * 网关设备认证测试：获取网关设备 Token
     */
    @Test
    public void testAuth() throws Exception {
        // 1.1 构建认证消息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testAuth][Codec: {}, 请求消息: {}, 数据包长度: {} 字节]", CODEC.type(), request, payload.length);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testAuth][响应消息: {}]", response);
                log.info("[testAuth][请将返回的 token 复制到 GATEWAY_TOKEN 常量中]");
            } else {
                log.warn("[testAuth][未收到响应]");
            }
        }
    }

    // ===================== 拓扑管理测试 =====================

    /**
     * 添加子设备拓扑关系测试
     * <p>
     * 网关设备向平台上报需要绑定的子设备信息
     */
    @Test
    public void testTopoAdd() throws Exception {
        // 1.1 构建子设备认证信息
        IotDeviceAuthReqDTO subAuthInfo = IotDeviceAuthUtils.getAuthInfo(
                SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME, SUB_DEVICE_SECRET);
        IotDeviceAuthReqDTO subDeviceAuth = new IotDeviceAuthReqDTO()
                .setClientId(subAuthInfo.getClientId())
                .setUsername(subAuthInfo.getUsername())
                .setPassword(subAuthInfo.getPassword());
        // 1.2 构建请求参数
        IotDeviceTopoAddReqDTO params = new IotDeviceTopoAddReqDTO();
        params.setSubDevices(Collections.singletonList(subDeviceAuth));
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.TOPO_ADD.getMethod(),
                withToken(params),
                null, null, null);
        // 1.3 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testTopoAdd][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testTopoAdd][响应消息: {}]", response);
            } else {
                log.warn("[testTopoAdd][未收到响应]");
            }
        }
    }

    /**
     * 删除子设备拓扑关系测试
     * <p>
     * 网关设备向平台上报需要解绑的子设备信息
     */
    @Test
    public void testTopoDelete() throws Exception {
        // 1.1 构建请求参数
        IotDeviceTopoDeleteReqDTO params = new IotDeviceTopoDeleteReqDTO();
        params.setSubDevices(Collections.singletonList(
                new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME)));
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod(),
                withToken(params),
                null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testTopoDelete][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testTopoDelete][响应消息: {}]", response);
            } else {
                log.warn("[testTopoDelete][未收到响应]");
            }
        }
    }

    /**
     * 获取子设备拓扑关系测试
     * <p>
     * 网关设备向平台查询已绑定的子设备列表
     */
    @Test
    public void testTopoGet() throws Exception {
        // 1.1 构建请求参数（目前为空，预留扩展）
        IotDeviceTopoGetReqDTO params = new IotDeviceTopoGetReqDTO();
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.TOPO_GET.getMethod(),
                withToken(params),
                null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testTopoGet][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testTopoGet][响应消息: {}]", response);
            } else {
                log.warn("[testTopoGet][未收到响应]");
            }
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
    public void testSubDeviceRegister() throws Exception {
        // 1.1 构建请求参数
        IotSubDeviceRegisterReqDTO subDevice = new IotSubDeviceRegisterReqDTO();
        subDevice.setProductKey(SUB_DEVICE_PRODUCT_KEY);
        subDevice.setDeviceName("mougezishebei");
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod(),
                withToken(Collections.singletonList(subDevice)),
                null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testSubDeviceRegister][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testSubDeviceRegister][响应消息: {}]", response);
            } else {
                log.warn("[testSubDeviceRegister][未收到响应]");
            }
        }
    }

    // ===================== 批量上报测试 =====================

    /**
     * 批量上报属性测试（网关 + 子设备）
     * <p>
     * 网关设备批量上报自身属性、事件，以及子设备的属性、事件
     */
    @Test
    public void testPropertyPackPost() throws Exception {
        // 1.1 构建【网关设备】自身属性
        Map<String, Object> gatewayProperties = MapUtil.<String, Object>builder()
                .put("temperature", 25.5)
                .build();
        // 1.2 构建【网关设备】自身事件
        IotDevicePropertyPackPostReqDTO.EventValue gatewayEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
        gatewayEvent.setValue(MapUtil.builder().put("message", "gateway started").build());
        gatewayEvent.setTime(System.currentTimeMillis());
        Map<String, IotDevicePropertyPackPostReqDTO.EventValue> gatewayEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                .put("statusReport", gatewayEvent)
                .build();
        // 1.3 构建【网关子设备】属性
        Map<String, Object> subDeviceProperties = MapUtil.<String, Object>builder()
                .put("power", 100)
                .build();
        // 1.4 构建【网关子设备】事件
        IotDevicePropertyPackPostReqDTO.EventValue subDeviceEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
        subDeviceEvent.setValue(MapUtil.builder().put("errorCode", 0).build());
        subDeviceEvent.setTime(System.currentTimeMillis());
        Map<String, IotDevicePropertyPackPostReqDTO.EventValue> subDeviceEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                .put("healthCheck", subDeviceEvent)
                .build();
        // 1.5 构建子设备数据
        IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData = new IotDevicePropertyPackPostReqDTO.SubDeviceData();
        subDeviceData.setIdentity(new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME));
        subDeviceData.setProperties(subDeviceProperties);
        subDeviceData.setEvents(subDeviceEvents);
        // 1.6 构建请求参数
        IotDevicePropertyPackPostReqDTO params = new IotDevicePropertyPackPostReqDTO();
        params.setProperties(gatewayProperties);
        params.setEvents(gatewayEvents);
        params.setSubDevices(List.of(subDeviceData));
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod(),
                withToken(params),
                null, null, null);
        // 1.7 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testPropertyPackPost][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testPropertyPackPost][响应消息: {}]", response);
            } else {
                log.warn("[testPropertyPackPost][未收到响应]");
            }
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 构建带 token 的 params
     * <p>
     * 返回格式：{token: "xxx", body: params}
     * - token：JWT 令牌
     * - body：实际请求内容（可以是 Map、List 或其他类型）
     *
     * @param params 原始参数（Map、List 或对象）
     * @return 包含 token 和 body 的 Map
     */
    private Map<String, Object> withToken(Object params) {
        Map<String, Object> result = new HashMap<>();
        result.put("token", GATEWAY_TOKEN);
        result.put("body", params);
        return result;
    }

}
