package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.hutool.core.collection.ListUtil;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * IoT 网关设备 TCP 协议集成测试（手动测试）
 *
 * <p>测试场景：网关设备（IotProductDeviceTypeEnum 的 GATEWAY 类型）通过 TCP 协议管理子设备拓扑关系
 *
 * <p>支持两种编解码格式：
 * <ul>
 *     <li>{@link IotTcpJsonDeviceMessageCodec} - JSON 格式</li>
 *     <li>{@link IotTcpBinaryDeviceMessageCodec} - 二进制格式</li>
 * </ul>
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（TCP 端口 8091）</li>
 *     <li>修改 {@link #CODEC} 选择测试的编解码格式</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testAuth()} - 网关设备认证</li>
 *             <li>{@link #testTopoAdd()} - 添加子设备拓扑关系</li>
 *             <li>{@link #testTopoDelete()} - 删除子设备拓扑关系</li>
 *             <li>{@link #testTopoGet()} - 获取子设备拓扑关系</li>
 *             <li>{@link #testSubDeviceRegister()} - 子设备动态注册</li>
 *             <li>{@link #testPropertyPackPost()} - 批量上报属性（网关 + 子设备）</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * <p>注意：TCP 协议是有状态的长连接，认证成功后同一连接上的后续请求无需再携带认证信息
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
public class IotGatewayDeviceTcpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8091;
    private static final int TIMEOUT_MS = 5000;

    // ===================== 编解码器选择（修改此处切换 JSON / Binary） =====================

    private static final IotDeviceMessageCodec CODEC = new IotTcpJsonDeviceMessageCodec();
//    private static final IotDeviceMessageCodec CODEC = new IotTcpBinaryDeviceMessageCodec();

    // ===================== 网关设备信息（根据实际情况修改，从 iot_device 表查询网关设备） =====================

    private static final String GATEWAY_PRODUCT_KEY = "m6XcS1ZJ3TW8eC0v";
    private static final String GATEWAY_DEVICE_NAME = "sub-ddd";
    private static final String GATEWAY_DEVICE_SECRET = "b3d62c70f8a4495487ed1d35d61ac2b3";

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
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testAuth][响应消息: {}]", response);
            } else {
                log.warn("[testAuth][未收到响应]");
            }
        }
    }

    // ===================== 拓扑管理测试 =====================

    /**
     * 添加子设备拓扑关系测试
     */
    @Test
    public void testTopoAdd() throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);

            // 1. 先进行认证
            IotDeviceMessage authResponse = authenticate(socket);
            log.info("[testTopoAdd][认证响应: {}]", authResponse);

            // 2.1 构建子设备认证信息
            IotDeviceAuthReqDTO subAuthInfo = IotDeviceAuthUtils.getAuthInfo(
                    SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME, SUB_DEVICE_SECRET);
            IotDeviceAuthReqDTO subDeviceAuth = new IotDeviceAuthReqDTO()
                    .setClientId(subAuthInfo.getClientId())
                    .setUsername(subAuthInfo.getUsername())
                    .setPassword(subAuthInfo.getPassword());
            // 2.2 构建请求参数
            IotDeviceTopoAddReqDTO params = new IotDeviceTopoAddReqDTO();
            params.setSubDevices(Collections.singletonList(subDeviceAuth));
            IotDeviceMessage request = IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.TOPO_ADD.getMethod(),
                    params,
                    null, null, null);
            // 2.3 编码
            byte[] payload = CODEC.encode(request);
            log.info("[testTopoAdd][Codec: {}, 请求消息: {}]", CODEC.type(), request);

            // 3.1 发送请求
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 3.2 解码响应
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
     */
    @Test
    public void testTopoDelete() throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);

            // 1. 先进行认证
            IotDeviceMessage authResponse = authenticate(socket);
            log.info("[testTopoDelete][认证响应: {}]", authResponse);

            // 2.1 构建请求参数
            IotDeviceTopoDeleteReqDTO params = new IotDeviceTopoDeleteReqDTO();
            params.setSubDevices(Collections.singletonList(
                    new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME)));
            IotDeviceMessage request = IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod(),
                    params,
                    null, null, null);
            // 2.2 编码
            byte[] payload = CODEC.encode(request);
            log.info("[testTopoDelete][Codec: {}, 请求消息: {}]", CODEC.type(), request);

            // 3.1 发送请求
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 3.2 解码响应
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
     */
    @Test
    public void testTopoGet() throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);

            // 1. 先进行认证
            IotDeviceMessage authResponse = authenticate(socket);
            log.info("[testTopoGet][认证响应: {}]", authResponse);

            // 2.1 构建请求参数
            IotDeviceTopoGetReqDTO params = new IotDeviceTopoGetReqDTO();
            IotDeviceMessage request = IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.TOPO_GET.getMethod(),
                    params,
                    null, null, null);
            // 2.2 编码
            byte[] payload = CODEC.encode(request);
            log.info("[testTopoGet][Codec: {}, 请求消息: {}]", CODEC.type(), request);

            // 3.1 发送请求
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 3.2 解码响应
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
     */
    @Test
    public void testSubDeviceRegister() throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);

            // 1. 先进行认证
            IotDeviceMessage authResponse = authenticate(socket);
            log.info("[testSubDeviceRegister][认证响应: {}]", authResponse);

            // 2.1 构建请求参数
            IotSubDeviceRegisterReqDTO subDevice = new IotSubDeviceRegisterReqDTO();
            subDevice.setProductKey(SUB_DEVICE_PRODUCT_KEY);
            subDevice.setDeviceName("mougezishebei");
            IotDeviceMessage request = IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod(),
                    Collections.singletonList(subDevice),
                    null, null, null);
            // 2.2 编码
            byte[] payload = CODEC.encode(request);
            log.info("[testSubDeviceRegister][Codec: {}, 请求消息: {}]", CODEC.type(), request);

            // 3.1 发送请求
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 3.2 解码响应
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
     */
    @Test
    public void testPropertyPackPost() throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);

            // 1. 先进行认证
            IotDeviceMessage authResponse = authenticate(socket);
            log.info("[testPropertyPackPost][认证响应: {}]", authResponse);

            // 2.1 构建【网关设备】自身属性
            Map<String, Object> gatewayProperties = MapUtil.<String, Object>builder()
                    .put("temperature", 25.5)
                    .build();
            // 2.2 构建【网关设备】自身事件
            IotDevicePropertyPackPostReqDTO.EventValue gatewayEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
            gatewayEvent.setValue(MapUtil.builder().put("message", "gateway started").build());
            gatewayEvent.setTime(System.currentTimeMillis());
            Map<String, IotDevicePropertyPackPostReqDTO.EventValue> gatewayEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                    .put("statusReport", gatewayEvent)
                    .build();
            // 2.3 构建【网关子设备】属性
            Map<String, Object> subDeviceProperties = MapUtil.<String, Object>builder()
                    .put("power", 100)
                    .build();
            // 2.4 构建【网关子设备】事件
            IotDevicePropertyPackPostReqDTO.EventValue subDeviceEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
            subDeviceEvent.setValue(MapUtil.builder().put("errorCode", 0).build());
            subDeviceEvent.setTime(System.currentTimeMillis());
            Map<String, IotDevicePropertyPackPostReqDTO.EventValue> subDeviceEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                    .put("healthCheck", subDeviceEvent)
                    .build();
            // 2.5 构建子设备数据
            IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData = new IotDevicePropertyPackPostReqDTO.SubDeviceData();
            subDeviceData.setIdentity(new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME));
            subDeviceData.setProperties(subDeviceProperties);
            subDeviceData.setEvents(subDeviceEvents);
            // 2.6 构建请求参数
            IotDevicePropertyPackPostReqDTO params = new IotDevicePropertyPackPostReqDTO();
            params.setProperties(gatewayProperties);
            params.setEvents(gatewayEvents);
            params.setSubDevices(ListUtil.of(subDeviceData));
            IotDeviceMessage request = IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod(),
                    params,
                    null, null, null);
            // 2.7 编码
            byte[] payload = CODEC.encode(request);
            log.info("[testPropertyPackPost][Codec: {}, 请求消息: {}]", CODEC.type(), request);

            // 3.1 发送请求
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 3.2 解码响应
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
     * 执行网关设备认证
     *
     * @param socket TCP 连接
     * @return 认证响应消息
     */
    private IotDeviceMessage authenticate(Socket socket) throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);
        byte[] payload = CODEC.encode(request);
        byte[] responseBytes = sendAndReceive(socket, payload);
        if (responseBytes != null) {
            return CODEC.decode(responseBytes);
        }
        return null;
    }

    /**
     * 发送 TCP 请求并接收响应
     *
     * @param socket  TCP Socket
     * @param payload 请求数据
     * @return 响应数据
     */
    private byte[] sendAndReceive(Socket socket, byte[] payload) throws Exception {
        // 1. 发送请求
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        out.write(payload);
        out.flush();

        // 2.1 等待一小段时间让服务器处理
        Thread.sleep(100);
        // 2.2 接收响应
        byte[] buffer = new byte[4096];
        try {
            int length = in.read(buffer);
            if (length > 0) {
                byte[] response = new byte[length];
                System.arraycopy(buffer, 0, response, 0, length);
                return response;
            }
            return null;
        } catch (java.net.SocketTimeoutException e) {
            log.warn("[sendAndReceive][接收响应超时]");
            return null;
        }
    }

}
