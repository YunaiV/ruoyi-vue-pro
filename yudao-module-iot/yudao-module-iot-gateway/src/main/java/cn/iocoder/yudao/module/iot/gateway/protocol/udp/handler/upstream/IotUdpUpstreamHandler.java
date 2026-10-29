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
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager.IotUdpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.lang.Assert;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_AUTH_FAIL;

/**
 * UDP 上行消息处理器
 * <p>
 * 采用无状态 Token 机制（每次请求携带 token）：
 * 1. 认证请求：设备发送 auth 消息，携带 clientId、username、password
 * 2. 返回 Token：服务端验证后返回 JWT token
 * 3. 后续请求：每次请求在 params 中携带 token
 * 4. 服务端验证：每次请求通过 IotDeviceTokenService.verifyToken() 验证
 * <p>
 * 透传（raw）报文按首字节区分：'{' 是 JSON，0x7E 是二进制序列化，0x49 0x52（"IR"）是透传：
 * <ul>
 *     <li>上行：0x49 0x52 | 0x01 | tokenLen(u16 BE) | token(UTF-8) | payload(1..4096)，每个包都携带 token，不凭地址识别设备；</li>
 *     <li>下行：0x49 0x52 | 0x01 | 0x0000 | payload。</li>
 * </ul>
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpUpstreamHandler {

    private static final String AUTH_METHOD = "auth";

    /**
     * 透传报文魔术字：'I'、'R'
     */
    private static final byte RAW_MAGIC_0 = 0x49;
    private static final byte RAW_MAGIC_1 = 0x52;
    /**
     * 透传报文版本
     */
    private static final byte RAW_VERSION = 0x01;
    /**
     * 透传报文头长度：魔术字(2) + 版本(1) + tokenLen(2)
     */
    private static final int RAW_HEADER_LENGTH = 5;
    /**
     * 业务消息、透传上报最多排队的包数：超过时丢弃
     */
    private static final int MAX_PENDING = 1024;

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

    /**
     * 业务消息、透传上报的 worker 队列（无序、有界）：在包到达时，按 socket 所在的 context 创建
     */
    private IotWorkerTaskQueue queue;

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
        // 1.2 透传报文（"IR" 开头）：每个包都携带 token，跳过反序列化，按数据解析脚本解析
        byte[] data = buffer.getBytes();
        if (isRawPacket(data)) {
            handleRawRequest(data, senderAddress, socket);
            return;
        }
        // 1.3 反序列化消息
        IotDeviceMessage message = serializer.deserialize(data);
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
        // 2. 获取设备、更新会话地址、发送消息到消息总线：会阻塞，交给 worker 执行（UDP 本身不保证顺序，包之间并行）；
        //    成功不应答（与原来一致），失败回到 event loop 应答错误
        Object params = body;
        getQueue().submit(() -> {
            // 2.1 获取设备详细信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(),
                    deviceInfo.getDeviceName());
            if (device == null) {
                log.warn("[handleBusinessRequest][设备不存在，来源: {}，productKey: {}，deviceName: {}]",
                        addressKey, deviceInfo.getProductKey(), deviceInfo.getDeviceName());
                throw invalidParamException("设备不存在");
            }
            // 2.2 更新会话地址（如有变化）
            sessionManager.updateSessionAddress(device.getId(), senderAddress);
            // 2.3 将 body 设置为实际的 params，发送消息到消息总线
            message.setParams(params);
            deviceMessageService.sendDeviceMessage(message, device.getProductKey(),
                    device.getDeviceName(), serverId);
            log.debug("[handleBusinessRequest][业务消息处理成功，设备 ID: {}，方法: {}，来源: {}]",
                    device.getId(), message.getMethod(), addressKey);
            return null;
        }).onFailure(e -> handleBusinessFailure(message, senderAddress, socket, e));
    }

    /**
     * 业务消息在 worker 上处理失败（event loop 上执行）：与同步处理时的异常应答一致
     *
     * @param message       消息信息
     * @param senderAddress 发送者地址
     * @param socket        UDP Socket
     * @param e             异常
     */
    private void handleBusinessFailure(IotDeviceMessage message, InetSocketAddress senderAddress,
                                       DatagramSocket socket, Throwable e) {
        String addressKey = sessionManager.buildAddressKey(senderAddress);
        if (e instanceof RejectedExecutionException) { // 队列已满：丢弃，应答稍后重试
            log.warn("[handleBusinessFailure][业务消息积压，丢弃，来源: {}，错误: {}]", addressKey, e.getMessage());
            sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                    TOO_MANY_REQUESTS.getCode(), TOO_MANY_REQUESTS.getMsg());
            return;
        }
        if (e instanceof ServiceException) {
            log.warn("[handleBusinessFailure][业务异常，来源: {}，requestId: {}，method: {}，错误: {}]",
                    addressKey, message.getRequestId(), message.getMethod(), e.getMessage());
            sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                    ((ServiceException) e).getCode(), e.getMessage());
            return;
        }
        if (e instanceof IllegalArgumentException) {
            log.warn("[handleBusinessFailure][参数校验失败，来源: {}，requestId: {}，method: {}，错误: {}]",
                    addressKey, message.getRequestId(), message.getMethod(), e.getMessage());
            sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                    BAD_REQUEST.getCode(), e.getMessage());
            return;
        }
        log.error("[handleBusinessFailure][处理消息失败，来源: {}，requestId: {}，method: {}]",
                addressKey, message.getRequestId(), message.getMethod(), e);
        sendErrorResponse(socket, senderAddress, message.getRequestId(), message.getMethod(),
                INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
    }

    /**
     * 处理透传上报：校验报文和 token 后，原始字节按产品的数据解析脚本 decode 后发送
     * <p>
     * 报文格式不正确、token 无效或已过期、设备不存在：只记日志并丢弃（不回复 JSON 错误响应）
     *
     * @param data          透传报文
     * @param senderAddress 发送者地址
     * @param socket        UDP Socket
     */
    private void handleRawRequest(byte[] data, InetSocketAddress senderAddress, DatagramSocket socket) {
        String addressKey = sessionManager.buildAddressKey(senderAddress);
        // 1.1 解析透传报文
        RawPacket packet;
        try {
            packet = decodeRawPacket(data);
        } catch (IllegalArgumentException e) {
            log.warn("[handleRawRequest][透传报文格式不正确，来源: {}，错误: {}]", addressKey, e.getMessage());
            return;
        }
        // 1.2 验证 token，获取设备信息
        IotDeviceIdentity deviceInfo;
        try {
            deviceInfo = deviceTokenService.verifyToken(packet.getToken());
        } catch (Exception e) {
            log.warn("[handleRawRequest][token 无效或已过期，来源: {}，错误: {}]", addressKey, e.getMessage());
            return;
        }
        if (deviceInfo == null) {
            log.warn("[handleRawRequest][token 无效或已过期，来源: {}]", addressKey);
            return;
        }
        // 2. 获取设备、更新会话地址、按产品的数据解析脚本 decode 后发送：会阻塞，交给 worker 执行（UDP 本身不保证顺序，包之间并行）
        IotDeviceIdentity identity = deviceInfo;
        byte[] payload = packet.getPayload();
        // 截止时间从收到（入队之前）开始计算
        long receivedNanos = System.nanoTime();
        getQueue().submit(() -> {
            // 2.1 获取设备详细信息：不存在时只记日志并丢弃
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(identity.getProductKey(),
                    identity.getDeviceName());
            if (device == null) {
                log.warn("[handleRawRequest][设备不存在，来源: {}，productKey: {}，deviceName: {}]",
                        addressKey, identity.getProductKey(), identity.getDeviceName());
                return null;
            }
            // 2.2 更新会话地址（如有变化）
            sessionManager.updateSessionAddress(device.getId(), senderAddress);
            // 2.3 decode 后发送
            return deviceMessageService.sendRawMessage(payload, device.getProductKey(),
                    device.getDeviceName(), serverId, IotProtocolTypeEnum.UDP, receivedNanos);
        }).onFailure(e -> log.warn("[handleRawRequest][透传上报处理失败，来源: {}，数据长度: {} 字节，错误: {}]",
                addressKey, payload.length, e.getMessage()));
    }

    /**
     * 获取 worker 队列：按当前（socket 所在的）context 创建；协议重启后 context 变化时重建
     */
    private IotWorkerTaskQueue getQueue() {
        Context context = Vertx.currentContext();
        if (queue == null || queue.getContext() != context) {
            queue = new IotWorkerTaskQueue(context, false, MAX_PENDING);
        }
        return queue;
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
            socket.send(Buffer.buffer(serializedData), address.getPort(), address.getHostString()).onComplete(result -> {
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

    // ===================== 透传报文编解码 =====================

    /**
     * 是否为透传报文：以 0x49 0x52（"IR"）开头
     *
     * @param data 数据包
     * @return 是否为透传报文
     */
    public static boolean isRawPacket(byte[] data) {
        return data != null && data.length >= 2 && data[0] == RAW_MAGIC_0 && data[1] == RAW_MAGIC_1;
    }

    /**
     * 解码透传上行报文：0x49 0x52 | 0x01 | tokenLen(u16 BE) | token(UTF-8) | payload(1..4096)
     *
     * @param data 数据包
     * @return 透传报文
     * @throws IllegalArgumentException 格式或长度不正确
     */
    public static RawPacket decodeRawPacket(byte[] data) {
        // 1. 校验报文头
        Assert.isTrue(isRawPacket(data), "透传报文魔术字不正确");
        Assert.isTrue(data.length >= RAW_HEADER_LENGTH, "透传报文长度不足");
        Assert.isTrue(data[2] == RAW_VERSION, "透传报文版本不支持: {}", data[2]);
        // 2.1 解析 token
        int tokenLength = ((data[3] & 0xFF) << 8) | (data[4] & 0xFF);
        Assert.isTrue(tokenLength > 0, "透传报文缺少 token");
        Assert.isTrue(data.length > RAW_HEADER_LENGTH + tokenLength, "透传报文缺少 payload");
        String token = new String(data, RAW_HEADER_LENGTH, tokenLength, StandardCharsets.UTF_8);
        // 2.2 解析 payload
        int payloadLength = data.length - RAW_HEADER_LENGTH - tokenLength;
        Assert.isTrue(payloadLength <= IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES,
                "透传报文 payload 超过 {} 字节", IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES);
        byte[] payload = Arrays.copyOfRange(data, RAW_HEADER_LENGTH + tokenLength, data.length);
        return new RawPacket(token, payload);
    }

    /**
     * 编码透传下行报文：0x49 0x52 | 0x01 | 0x0000 | payload（下行不携带 token）
     *
     * @param payload 原始字节
     * @return 透传报文
     */
    public static byte[] encodeRawDownstream(byte[] payload) {
        byte[] data = new byte[RAW_HEADER_LENGTH + payload.length];
        data[0] = RAW_MAGIC_0;
        data[1] = RAW_MAGIC_1;
        data[2] = RAW_VERSION;
        // data[3]、data[4]：tokenLen = 0
        System.arraycopy(payload, 0, data, RAW_HEADER_LENGTH, payload.length);
        return data;
    }

    /**
     * 透传上行报文
     */
    @Getter
    @AllArgsConstructor
    public static class RawPacket {

        /**
         * 设备 token
         */
        private final String token;
        /**
         * 原始字节
         */
        private final byte[] payload;

    }

}
