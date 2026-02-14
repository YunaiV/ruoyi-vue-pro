package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
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
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.json.IotJsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * IoT 网关设备 UDP 协议集成测试（手动测试）
 *
 * <p>测试场景：网关设备（IotProductDeviceTypeEnum 的 GATEWAY 类型）通过 UDP 协议管理子设备拓扑关系
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（UDP 端口 8093）</li>
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

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8093;
    private static final int TIMEOUT_MS = 5000;

    // ===================== 序列化器 =====================

    /**
     * 消息序列化器
     */
    private static final IotMessageSerializer SERIALIZER = new IotJsonSerializer();

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
        // 1. 构建认证消息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        IotDeviceMessage request = IotDeviceMessage.requestOf("auth", authReqDTO);

        // 2. 发送并接收响应
        IotDeviceMessage response = sendAndReceive(request);
        log.info("[testAuth][响应消息: {}]", response);
        log.info("[testAuth][请将返回的 token 复制到 GATEWAY_TOKEN 常量中]");
    }

    // ===================== 拓扑管理测试 =====================

    /**
     * 添加子设备拓扑关系测试
     */
    @Test
    public void testTopoAdd() throws Exception {
        // 1. 构建子设备认证信息
        IotDeviceAuthReqDTO subAuthInfo = IotDeviceAuthUtils.getAuthInfo(
                SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME, SUB_DEVICE_SECRET);
        IotDeviceAuthReqDTO subDeviceAuth = new IotDeviceAuthReqDTO()
                .setClientId(subAuthInfo.getClientId())
                .setUsername(subAuthInfo.getUsername())
                .setPassword(subAuthInfo.getPassword());
        IotDeviceTopoAddReqDTO params = new IotDeviceTopoAddReqDTO();
        params.setSubDevices(Collections.singletonList(subDeviceAuth));
        IotDeviceMessage request = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.TOPO_ADD.getMethod(), withToken(params));

        // 2. 发送并接收响应
        IotDeviceMessage response = sendAndReceive(request);
        log.info("[testTopoAdd][响应消息: {}]", response);
    }

    /**
     * 删除子设备拓扑关系测试
     */
    @Test
    public void testTopoDelete() throws Exception {
        // 1. 构建请求参数
        IotDeviceTopoDeleteReqDTO params = new IotDeviceTopoDeleteReqDTO();
        params.setSubDevices(Collections.singletonList(
                new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME)));
        IotDeviceMessage request = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod(), withToken(params));

        // 2. 发送并接收响应
        IotDeviceMessage response = sendAndReceive(request);
        log.info("[testTopoDelete][响应消息: {}]", response);
    }

    /**
     * 获取子设备拓扑关系测试
     */
    @Test
    public void testTopoGet() throws Exception {
        // 1. 构建请求参数
        IotDeviceTopoGetReqDTO params = new IotDeviceTopoGetReqDTO();
        IotDeviceMessage request = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.TOPO_GET.getMethod(), withToken(params));

        // 2. 发送并接收响应
        IotDeviceMessage response = sendAndReceive(request);
        log.info("[testTopoGet][响应消息: {}]", response);
    }

    // ===================== 子设备注册测试 =====================

    /**
     * 子设备动态注册测试
     */
    @Test
    public void testSubDeviceRegister() throws Exception {
        // 1. 构建请求参数
        IotSubDeviceRegisterReqDTO subDevice = new IotSubDeviceRegisterReqDTO();
        subDevice.setProductKey(SUB_DEVICE_PRODUCT_KEY);
        subDevice.setDeviceName("mougezishebei");
        IotDeviceMessage request = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod(),
                withToken(Collections.singletonList(subDevice)));

        // 2. 发送并接收响应
        IotDeviceMessage response = sendAndReceive(request);
        log.info("[testSubDeviceRegister][响应消息: {}]", response);
    }

    // ===================== 批量上报测试 =====================

    /**
     * 批量上报属性测试（网关 + 子设备）
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
        params.setSubDevices(ListUtil.of(subDeviceData));
        IotDeviceMessage request = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod(), withToken(params));

        // 2. 发送并接收响应
        IotDeviceMessage response = sendAndReceive(request);
        log.info("[testPropertyPackPost][响应消息: {}]", response);
    }

    // ===================== 辅助方法 =====================

    /**
     * 构建带 token 的 params
     */
    private Map<String, Object> withToken(Object params) {
        Map<String, Object> result = new HashMap<>();
        result.put("token", GATEWAY_TOKEN);
        result.put("body", params);
        return result;
    }

    /**
     * 发送 UDP 消息并接收响应
     */
    private IotDeviceMessage sendAndReceive(IotDeviceMessage request) throws Exception {
        byte[] payload = SERIALIZER.serialize(request);
        log.info("[sendAndReceive][发送消息: {}，数据长度: {} 字节]", request.getMethod(), payload.length);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress address = InetAddress.getByName(SERVER_HOST);
            DatagramPacket sendPacket = new DatagramPacket(payload, payload.length, address, SERVER_PORT);
            socket.send(sendPacket);

            byte[] receiveData = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(receivePacket);
                byte[] responseBytes = new byte[receivePacket.getLength()];
                System.arraycopy(receivePacket.getData(), 0, responseBytes, 0, receivePacket.getLength());
                return SERIALIZER.deserialize(responseBytes);
            } catch (java.net.SocketTimeoutException e) {
                log.warn("[sendAndReceive][接收响应超时]");
                return null;
            }
        }
    }

}
