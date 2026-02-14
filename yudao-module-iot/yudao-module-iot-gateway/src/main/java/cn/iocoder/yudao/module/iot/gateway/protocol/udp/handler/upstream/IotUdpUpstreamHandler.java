package cn.iocoder.yudao.module.iot.gateway.protocol.udp.handler.upstream;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager.IotUdpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.lang.Assert;

import java.net.InetSocketAddress;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_AUTH_FAIL;

/**
 * UDP 上行消息处理器
 * <p>
 * 采用无状态 Token 机制（每次请求携带 token）：
 * 1. 认证请求：设备发送 auth 消息，携带 clientId、username、password
 * 2. 返回 Token：服务端验证后返回 JWT token
 * 3. 后续请求：每次请求在 params 中携带 token
 * 4. 服务端验证：每次请求通过 IotDeviceTokenService.verifyToken() 验证
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpUpstreamHandler {

    private static final String AUTH_METHOD = "auth";

    /**
     * Token 参数 Key
     */
    private static final String PARAM_KEY_TOKEN = "token";
    /**
     * Body 参数 Key（实际请求内容）
     */
    private static final String PARAM_KEY_BODY = "body";

    private final String serverId;

    /**
     * 消息序列化器（处理业务消息序列化/反序列化）
     */
    private final IotMessageSerializer serializer;
    /**
     * UDP 会话管理器
     */
    private final IotUdpSessionManager sessionManager;

    private final IotDeviceMessageService deviceMessageService;
    private final IotDeviceService deviceService;
    private final IotDeviceTokenService deviceTokenService;
    private final IotDeviceCommonApi deviceApi;

    public IotUdpUpstreamHandler(String serverId,
                                 IotUdpSessionManager sessionManager,
                                 IotMessageSerializer serializer) {
        Assert.notNull(serializer, "消息序列化器必须配置");
        Assert.notNull(sessionManager, "会话管理器不能为空");
        this.serverId = serverId;
        this.sessionManager = sessionManager;
        this.serializer = serializer;
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.deviceService = SpringUtil.getBean(IotDeviceService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);
    }

    /**
     * 处理 UDP 数据包
     *
     * @param packet 数据包
     * @param socket UDP Socket
     */
    public void handle(DatagramPacket packet, DatagramSocket socket) {
        InetSocketAddress senderAddress = new InetSocketAddress(packet.sender().host(), packet.sender().port());
        Buffer data = packet.data();
        String addressKey = sessionManager.buildAddressKey(senderAddress);
        log.debug("[handle][收到 UDP 数据包，来源: {}，数据长度: {} 字节]", addressKey, data.length());
        processMessage(data, senderAddress, socket);
    }

    /**
     * 处理消息
     *
     * @param buffer        消息
     * @param senderAddress 发送者地址
     * @param socket        UDP Socket
     */
    private void processMessage(Buffer buffer, InetSocketAddress senderAddress, DatagramSocket socket) {
        String addressKey = sessionManager.buildAddressKey(senderAddress);
        // 1.1 基础检查
        if (ArrayUtil.isEmpty(buffer)) {
            return;
        }
        // 1.2 反序列化消息
        IotDeviceMessage message = serializer.deserialize(buffer.getBytes());
        if (message == null) {
            sendErrorResponse(socket, senderAddress, null, null, BAD_REQUEST.getCode(), "消息反序列化失败");
            return;
        }

        // 2. 根据消息类型路由处理
        try {
            if (AUTH_METHOD.equals(message.getMethod())) {
                // 认证请求
                handleAuthenticationRequest(message, senderAddress, socket);
            } else if (IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod().equals(message.getMethod())) {
                // 设备动态注册请求
                handleRegisterRequest(message, senderAddress, socket);
            } else {
                // 业务消息
                handleBusinessRequest(message, senderAddress, socket);
            }
        } catch (ServiceException e) {
            // 业务异常，返回对应的错误码和错误信息
            log.warn("[processMessage][业务异常，来源: {}，requestId: {}，method: {}，错误: {}]",
                    addressKey, message.getRequestId(), message.getMethod(), e.getMessage());
            sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                    e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            // 参数校验失败，返回 400
            log.warn("[processMessage][参数校验失败，来源: {}，requestId: {}，method: {}，错误: {}]",
                    addressKey, message.getRequestId(), message.getMethod(), e.getMessage());
            sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                    BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            // 其他异常，返回 500
            log.error("[processMessage][处理消息失败，来源: {}，requestId: {}，method: {}]",
                    addressKey, message.getRequestId(), message.getMethod(), e);
            sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                    INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
        }
    }

    /**
     * 处理认证请求
     *
     * @param message       消息信息
     * @param senderAddress 发送者地址
     * @param socket        UDP Socket
     */
    @SuppressWarnings("DuplicatedCode")
    private void handleAuthenticationRequest(IotDeviceMessage message, InetSocketAddress senderAddress,
                                             DatagramSocket socket) {
        String clientId = IdUtil.simpleUUID();
        // 1. 解析认证参数
        IotDeviceAuthReqDTO authParams = JsonUtils.convertObject(message.getParams(), IotDeviceAuthReqDTO.class);
        Assert.notNull(authParams, "认证参数不能为空");
        Assert.notBlank(authParams.getUsername(), "username 不能为空");
        Assert.notBlank(authParams.getPassword(), "password 不能为空");

        // 2.1 执行认证
        CommonResult<Boolean> authResult = deviceApi.authDevice(authParams);
        authResult.checkError();
        if (!BooleanUtil.isTrue(authResult.getData())) {
            throw exception(DEVICE_AUTH_FAIL);
        }
        // 2.2 解析设备信息
        IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(authParams.getUsername());
        Assert.notNull(deviceInfo, "解析设备信息失败");
        // 2.3 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(),
                deviceInfo.getDeviceName());
        Assert.notNull(device, "设备不存在");

        // 3. 生成 JWT Token（无状态）
        String token = deviceTokenService.createToken(device.getProductKey(), device.getDeviceName());

        // 4.1 注册会话
        registerSession(senderAddress, device, clientId);
        // 4.2 发送上线消息
        sendOnlineMessage(device);
        // 4.3 发送成功响应（包含 token）
        sendSuccessResponse(socket, senderAddress, message.getRequestId(), AUTH_METHOD,
                MapUtil.of("token", token));
        log.info("[handleAuthenticationRequest][认证成功，设备 ID: {}，设备名: {}，来源: {}]",
                device.getId(), device.getDeviceName(), sessionManager.buildAddressKey(senderAddress));
    }

    /**
     * 处理设备动态注册请求（一型一密，不需要认证）
     *
     * @param message       消息信息
     * @param senderAddress 发送者地址
     * @param socket        UDP Socket
     * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
     */
    @SuppressWarnings("DuplicatedCode")
    private void handleRegisterRequest(IotDeviceMessage message, InetSocketAddress senderAddress,
                                       DatagramSocket socket) {
        // 1. 解析注册参数
        IotDeviceRegisterReqDTO params = JsonUtils.convertObject(message.getParams(), IotDeviceRegisterReqDTO.class);
        Assert.notNull(params, "注册参数不能为空");
        Assert.notBlank(params.getProductKey(), "productKey 不能为空");
        Assert.notBlank(params.getDeviceName(), "deviceName 不能为空");
        Assert.notBlank(params.getSign(), "sign 不能为空");

        // 2. 调用动态注册
        CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(params);
        result.checkError();

        // 3. 发送成功响应
        sendSuccessResponse(socket, senderAddress, message.getRequestId(),
                IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), result.getData());
        log.info("[handleRegisterRequest][注册成功，来源: {}，设备名: {}]",
                sessionManager.buildAddressKey(senderAddress), params.getDeviceName());
    }

    /**
     * 处理业务请求
     * <p>
     * 请求参数格式：
     * - token：JWT 令牌
     * - body：实际请求内容（可以是 Map、List 或其他类型）
     *
     * @param message       消息信息
     * @param senderAddress 发送者地址
     * @param socket        UDP Socket
     */
    @SuppressWarnings("unchecked")
    private void handleBusinessRequest(IotDeviceMessage message, InetSocketAddress senderAddress,
                                       DatagramSocket socket) {
        String addressKey = sessionManager.buildAddressKey(senderAddress);
        // 1.1 从消息中提取 token 和 body
        String token = null;
        Object body = null;
        if (message.getParams() instanceof Map) {
            Map<String, Object> paramsMap = (Map<String, Object>) message.getParams();
            token = (String) paramsMap.get(PARAM_KEY_TOKEN);
            body = paramsMap.get(PARAM_KEY_BODY);
        }
        if (StrUtil.isBlank(token)) {
            log.warn("[handleBusinessRequest][缺少 token，来源: {}]", addressKey);
            sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                    UNAUTHORIZED.getCode(), "请先进行认证");
            return;
        }
        // 1.2 验证 token，获取设备信息
        IotDeviceIdentity deviceInfo = deviceTokenService.verifyToken(token);
        if (deviceInfo == null) {
            log.warn("[handleBusinessRequest][token 无效或已过期，来源: {}]", addressKey);
            sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                    UNAUTHORIZED.getCode(), "token 无效或已过期");
            return;
        }
        // 1.3 获取设备详细信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(),
                deviceInfo.getDeviceName());
        if (device == null) {
            log.warn("[handleBusinessRequest][设备不存在，来源: {}，productKey: {}，deviceName: {}]",
                    addressKey, deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                    BAD_REQUEST.getCode(), "设备不存在");
            return;
        }

        // 2. 更新会话地址（如有变化）
        sessionManager.updateSessionAddress(device.getId(), senderAddress);

        // 3. 将 body 设置为实际的 params，发送消息到消息总线
        message.setParams(body);
        deviceMessageService.sendDeviceMessage(message, device.getProductKey(),
                device.getDeviceName(), serverId);
        log.debug("[handleBusinessRequest][业务消息处理成功，设备 ID: {}，方法: {}，来源: {}]",
                device.getId(), message.getMethod(), addressKey);
    }

    /**
     * 注册会话信息
     *
     * @param address  设备地址
     * @param device   设备
     * @param clientId 客户端 ID
     */
    private void registerSession(InetSocketAddress address, IotDeviceRespDTO device, String clientId) {
        IotUdpSessionManager.SessionInfo sessionInfo = new IotUdpSessionManager.SessionInfo()
                .setDeviceId(device.getId())
                .setProductKey(device.getProductKey())
                .setDeviceName(device.getDeviceName())
                .setAddress(address);
        sessionManager.registerSession(device.getId(), sessionInfo);
    }

    /**
     * 发送设备上线消息
     *
     * @param device 设备信息
     */
    private void sendOnlineMessage(IotDeviceRespDTO device) {
        IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
        deviceMessageService.sendDeviceMessage(onlineMessage, device.getProductKey(),
                device.getDeviceName(), serverId);
    }

    // ===================== 发送响应消息 =====================

    /**
     * 发送成功响应
     *
     * @param socket    UDP Socket
     * @param address   目标地址
     * @param requestId 请求 ID
     * @param method    方法名
     * @param data      响应数据
     */
    private void sendSuccessResponse(DatagramSocket socket, InetSocketAddress address,
                                     String requestId, String method, Object data) {
        IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, data, SUCCESS.getCode(), null);
        writeResponse(socket, address, responseMessage);
    }

    /**
     * 发送错误响应
     *
     * @param socket    UDP Socket
     * @param address   目标地址
     * @param requestId 请求 ID
     * @param method    方法名
     * @param code      错误码
     * @param msg       错误消息
     */
    private void sendErrorResponse(DatagramSocket socket, InetSocketAddress address,
                                   String requestId, String method, Integer code, String msg) {
        IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, null, code, msg);
        writeResponse(socket, address, responseMessage);
    }

    /**
     * 写入响应到 Socket
     *
     * @param socket          UDP Socket
     * @param address         目标地址
     * @param responseMessage 响应消息
     */
    private void writeResponse(DatagramSocket socket, InetSocketAddress address, IotDeviceMessage responseMessage) {
        try {
            byte[] serializedData = serializer.serialize(responseMessage);
            socket.send(Buffer.buffer(serializedData), address.getPort(), address.getHostString(), result -> {
                if (result.failed()) {
                    log.error("[writeResponse][发送响应失败，地址: {}]",
                            sessionManager.buildAddressKey(address), result.cause());
                }
            });
        } catch (Exception e) {
            log.error("[writeResponse][发送响应异常，地址: {}]",
                    sessionManager.buildAddressKey(address), e);
        }
    }

}
