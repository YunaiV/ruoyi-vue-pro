package cn.iocoder.yudao.module.iot.gateway.protocol.udp.router;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpBinaryDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpJsonDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotUdpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager.IotUdpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;

// TODO @AI：注释里，不要出现 CoAP，避免理解成本过高；
/**
 * UDP 上行消息处理器
 * <p>
 * 采用 CoAP 风格的 Token 机制（无状态，每次请求携带 token）：
 * 1. 认证请求：设备发送 auth 消息，携带 clientId、username、password
 * 2. 返回 Token：服务端验证后返回 JWT token
 * 3. 后续请求：每次请求在 params 中携带 token
 * 4. 服务端验证：每次请求通过 IotDeviceTokenService.verifyToken() 验证
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpUpstreamHandler {

    private static final String CODEC_TYPE_JSON = IotTcpJsonDeviceMessageCodec.TYPE;
    private static final String CODEC_TYPE_BINARY = IotTcpBinaryDeviceMessageCodec.TYPE;

    private static final String AUTH_METHOD = "auth";

    private final IotDeviceMessageService deviceMessageService;

    private final IotDeviceService deviceService;

    private final IotUdpSessionManager sessionManager;

    private final IotDeviceTokenService deviceTokenService;

    private final IotDeviceCommonApi deviceApi;

    private final String serverId;

    public IotUdpUpstreamHandler(IotUdpUpstreamProtocol protocol,
                                 IotDeviceMessageService deviceMessageService,
                                 IotDeviceService deviceService,
                                 IotUdpSessionManager sessionManager) {
        this.deviceMessageService = deviceMessageService;
        this.deviceService = deviceService;
        this.sessionManager = sessionManager;
        this.deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.serverId = protocol.getServerId();
    }

    // TODO @AI：protocol 这个参数如果用不到，就删除下；
    /**
     * 处理 UDP 数据包
     *
     * @param packet   数据包
     * @param socket   UDP Socket
     * @param protocol UDP 协议
     */
    public void handle(DatagramPacket packet, DatagramSocket socket, IotUdpUpstreamProtocol protocol) {
        InetSocketAddress senderAddress = new InetSocketAddress(packet.sender().host(), packet.sender().port());
        Buffer data = packet.data();
        log.debug("[handle][收到 UDP 数据包，来源: {}，数据长度: {} 字节]",
                sessionManager.buildAddressKey(senderAddress), data.length());
        try {
            processMessage(data, senderAddress, socket);
        } catch (Exception e) {
            log.error("[handle][处理消息失败，来源: {}，错误: {}]",
                    sessionManager.buildAddressKey(senderAddress), e.getMessage(), e);
            // UDP 无连接，不需要断开连接，只记录错误
        }
    }

    /**
     * 处理消息
     *
     * @param buffer        消息
     * @param senderAddress 发送者地址
     * @param socket        UDP Socket
     */
    private void processMessage(Buffer buffer, InetSocketAddress senderAddress, DatagramSocket socket) {
        // 1. 基础检查
        if (buffer == null || buffer.length() == 0) {
            return;
        }

        // 2. 获取消息格式类型
        String codecType = getMessageCodecType(buffer);

        // 3. 解码消息
        IotDeviceMessage message;
        try {
            message = deviceMessageService.decodeDeviceMessage(buffer.getBytes(), codecType);
            if (message == null) {
                log.warn("[processMessage][消息解码失败，来源: {}]", sessionManager.buildAddressKey(senderAddress));
                sendErrorResponse(socket, senderAddress, null, "消息解码失败", codecType);
                return;
            }
        } catch (Exception e) {
            log.error("[processMessage][消息解码异常，来源: {}]", sessionManager.buildAddressKey(senderAddress), e);
            sendErrorResponse(socket, senderAddress, null, "消息解码失败: " + e.getMessage(), codecType);
            return;
        }

        // 4. 根据消息类型路由处理
        try {
            if (AUTH_METHOD.equals(message.getMethod())) {
                // 认证请求
                handleAuthenticationRequest(message, codecType, senderAddress, socket);
            } else {
                // 业务消息
                handleBusinessRequest(message, codecType, senderAddress, socket);
            }
        } catch (Exception e) {
            log.error("[processMessage][处理消息失败，来源: {}，消息方法: {}]",
                    sessionManager.buildAddressKey(senderAddress), message.getMethod(), e);
            sendErrorResponse(socket, senderAddress, message.getRequestId(), "消息处理失败", codecType);
        }
    }

    /**
     * 处理认证请求
     *
     * @param message       消息信息
     * @param codecType     消息编解码类型
     * @param senderAddress 发送者地址
     * @param socket        UDP Socket
     */
    private void handleAuthenticationRequest(IotDeviceMessage message, String codecType,
                                             InetSocketAddress senderAddress, DatagramSocket socket) {
        String addressKey = sessionManager.buildAddressKey(senderAddress);
        try {
            // 1.1 解析认证参数
            IotDeviceAuthReqDTO authParams = parseAuthParams(message.getParams());
            if (authParams == null) {
                log.warn("[handleAuthenticationRequest][认证参数解析失败，来源: {}]", addressKey);
                sendErrorResponse(socket, senderAddress, message.getRequestId(), "认证参数不完整", codecType);
                return;
            }
            // 1.2 执行认证
            if (!validateDeviceAuth(authParams)) {
                log.warn("[handleAuthenticationRequest][认证失败，来源: {}，username: {}]",
                        addressKey, authParams.getUsername());
                sendErrorResponse(socket, senderAddress, message.getRequestId(), "认证失败", codecType);
                return;
            }

            // 2.1 解析设备信息
            IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(authParams.getUsername());
            if (deviceInfo == null) {
                sendErrorResponse(socket, senderAddress, message.getRequestId(), "解析设备信息失败", codecType);
                return;
            }
            // 2.2 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(),
                    deviceInfo.getDeviceName());
            if (device == null) {
                sendErrorResponse(socket, senderAddress, message.getRequestId(), "设备不存在", codecType);
                return;
            }

            // 3.1 生成 JWT Token（CoAP 风格）
            String token = deviceTokenService.createToken(device.getProductKey(), device.getDeviceName());

            // 3.2 更新设备地址映射（用于下行消息）
            sessionManager.updateDeviceAddress(device.getId(), senderAddress);

            // 3.3 发送上线消息
            sendOnlineMessage(device);

            // 3.4 发送成功响应（包含 token）
            sendAuthSuccessResponse(socket, senderAddress, message.getRequestId(), token, codecType);
            log.info("[handleAuthenticationRequest][认证成功，设备 ID: {}，设备名: {}，来源: {}]",
                    device.getId(), device.getDeviceName(), addressKey);
        } catch (Exception e) {
            log.error("[handleAuthenticationRequest][认证处理异常，来源: {}]", addressKey, e);
            sendErrorResponse(socket, senderAddress, message.getRequestId(), "认证处理异常", codecType);
        }
    }

    /**
     * 处理业务请求
     *
     * @param message       消息信息
     * @param codecType     消息编解码类型
     * @param senderAddress 发送者地址
     * @param socket        UDP Socket
     */
    @SuppressWarnings("unchecked")
    private void handleBusinessRequest(IotDeviceMessage message, String codecType,
                                       InetSocketAddress senderAddress, DatagramSocket socket) {
        String addressKey = sessionManager.buildAddressKey(senderAddress);
        try {
            // TODO @AI：token 需要枚举个 KEY；考虑到是通过 params 传递的话，需要获取到后，从 map 里移除掉，避免影响后续业务逻辑处理；
            // 1. 从消息中提取 token（CoAP 风格：消息体携带 token）
            String token = null;
            if (message.getParams() instanceof Map) {
                token = MapUtil.getStr((Map<String, Object>) message.getParams(), "token");
            }

            if (StrUtil.isBlank(token)) {
                log.warn("[handleBusinessRequest][缺少 token，来源: {}]", addressKey);
                sendErrorResponse(socket, senderAddress, message.getRequestId(), "请先进行认证", codecType);
                return;
            }

            // 2. 验证 token，获取设备信息
            IotDeviceAuthUtils.DeviceInfo deviceInfo = deviceTokenService.verifyToken(token);
            if (deviceInfo == null) {
                log.warn("[handleBusinessRequest][token 无效或已过期，来源: {}]", addressKey);
                sendErrorResponse(socket, senderAddress, message.getRequestId(), "token 无效或已过期", codecType);
                return;
            }

            // 3. 获取设备详细信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(),
                    deviceInfo.getDeviceName());
            if (device == null) {
                log.warn("[handleBusinessRequest][设备不存在，来源: {}，productKey: {}，deviceName: {}]",
                        addressKey, deviceInfo.getProductKey(), deviceInfo.getDeviceName());
                sendErrorResponse(socket, senderAddress, message.getRequestId(), "设备不存在", codecType);
                return;
            }

            // 4. 更新设备地址映射（保持最新）
            sessionManager.updateDeviceAddress(device.getId(), senderAddress);

            // 5. 发送消息到消息总线
            deviceMessageService.sendDeviceMessage(message, device.getProductKey(),
                    device.getDeviceName(), serverId);

            // 6. 发送成功响应
            sendSuccessResponse(socket, senderAddress, message.getRequestId(), "处理成功", codecType);
            log.debug("[handleBusinessRequest][业务消息处理成功，设备 ID: {}，方法: {}，来源: {}]",
                    device.getId(), message.getMethod(), addressKey);
        } catch (Exception e) {
            log.error("[handleBusinessRequest][业务请求处理异常，来源: {}]", addressKey, e);
            sendErrorResponse(socket, senderAddress, message.getRequestId(), "处理失败", codecType);
        }
    }

    /**
     * 获取消息编解码类型
     *
     * @param buffer 消息
     * @return 消息编解码类型
     */
    private String getMessageCodecType(Buffer buffer) {
        // 检测消息格式类型
        return IotTcpBinaryDeviceMessageCodec.isBinaryFormatQuick(buffer.getBytes()) ? CODEC_TYPE_BINARY
                : CODEC_TYPE_JSON;
    }

    /**
     * 发送设备上线消息
     *
     * @param device 设备信息
     */
    private void sendOnlineMessage(IotDeviceRespDTO device) {
        try {
            IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
            deviceMessageService.sendDeviceMessage(onlineMessage, device.getProductKey(),
                    device.getDeviceName(), serverId);
        } catch (Exception e) {
            log.error("[sendOnlineMessage][发送上线消息失败，设备: {}]", device.getDeviceName(), e);
        }
    }

    /**
     * 验证设备认证信息
     *
     * @param authParams 认证参数
     * @return 是否认证成功
     */
    private boolean validateDeviceAuth(IotDeviceAuthReqDTO authParams) {
        try {
            CommonResult<Boolean> result = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                    .setClientId(authParams.getClientId()).setUsername(authParams.getUsername())
                    .setPassword(authParams.getPassword()));
            result.checkError();
            return BooleanUtil.isTrue(result.getData());
        } catch (Exception e) {
            log.error("[validateDeviceAuth][设备认证异常，username: {}]", authParams.getUsername(), e);
            return false;
        }
    }

    /**
     * 发送认证成功响应（包含 token）
     *
     * @param socket        UDP Socket
     * @param address       目标地址
     * @param requestId     请求 ID
     * @param token         JWT Token
     * @param codecType     消息编解码类型
     */
    private void sendAuthSuccessResponse(DatagramSocket socket, InetSocketAddress address,
                                         String requestId, String token, String codecType) {
        try {
            Object responseData = MapUtil.builder()
                    .put("success", true)
                    .put("token", token)
                    .put("message", "认证成功")
                    .build();

            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, AUTH_METHOD, responseData, 0, "认证成功");
            byte[] encodedData = deviceMessageService.encodeDeviceMessage(responseMessage, codecType);
            socket.send(Buffer.buffer(encodedData), address.getPort(), address.getHostString(), result -> {
                if (result.failed()) {
                    log.error("[sendAuthSuccessResponse][发送认证成功响应失败，地址: {}]",
                            sessionManager.buildAddressKey(address), result.cause());
                }
            });
        } catch (Exception e) {
            log.error("[sendAuthSuccessResponse][发送认证成功响应异常，地址: {}]",
                    sessionManager.buildAddressKey(address), e);
        }
    }

    /**
     * 发送成功响应
     *
     * @param socket        UDP Socket
     * @param address       目标地址
     * @param requestId     请求 ID
     * @param message       消息
     * @param codecType     消息编解码类型
     */
    @SuppressWarnings("SameParameterValue")
    private void sendSuccessResponse(DatagramSocket socket, InetSocketAddress address,
                                     String requestId, String message, String codecType) {
        sendResponse(socket, address, true, message, requestId, codecType);
    }

    /**
     * 发送错误响应
     *
     * @param socket        UDP Socket
     * @param address       目标地址
     * @param requestId     请求 ID
     * @param errorMessage  错误消息
     * @param codecType     消息编解码类型
     */
    private void sendErrorResponse(DatagramSocket socket, InetSocketAddress address,
                                   String requestId, String errorMessage, String codecType) {
        sendResponse(socket, address, false, errorMessage, requestId, codecType);
    }

    /**
     * 发送响应消息
     *
     * @param socket    UDP Socket
     * @param address   目标地址
     * @param success   是否成功
     * @param message   消息
     * @param requestId 请求 ID
     * @param codecType 消息编解码类型
     */
    private void sendResponse(DatagramSocket socket, InetSocketAddress address, boolean success,
                              String message, String requestId, String codecType) {
        try {
            Object responseData = MapUtil.builder()
                    .put("success", success)
                    .put("message", message)
                    .build();

            int code = success ? 0 : 401;
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, "response", responseData,
                    code, message);

            byte[] encodedData = deviceMessageService.encodeDeviceMessage(responseMessage, codecType);
            socket.send(Buffer.buffer(encodedData), address.getPort(), address.getHostString(), ar -> {
                if (ar.failed()) {
                    log.error("[sendResponse][发送响应失败，地址: {}]",
                            sessionManager.buildAddressKey(address), ar.cause());
                }
            });
        } catch (Exception e) {
            log.error("[sendResponse][发送响应异常，地址: {}]",
                    sessionManager.buildAddressKey(address), e);
        }
    }

    /**
     * 解析认证参数
     *
     * @param params 参数对象（通常为 Map 类型）
     * @return 认证参数 DTO，解析失败时返回 null
     */
    @SuppressWarnings("unchecked")
    private IotDeviceAuthReqDTO parseAuthParams(Object params) {
        if (params == null) {
            return null;
        }

        try {
            // 参数默认为 Map 类型，直接转换
            if (params instanceof Map) {
                Map<String, Object> paramMap = (Map<String, Object>) params;
                return new IotDeviceAuthReqDTO()
                        .setClientId(MapUtil.getStr(paramMap, "clientId"))
                        .setUsername(MapUtil.getStr(paramMap, "username"))
                        .setPassword(MapUtil.getStr(paramMap, "password"));
            }

            // 如果已经是目标类型，直接返回
            if (params instanceof IotDeviceAuthReqDTO) {
                return (IotDeviceAuthReqDTO) params;
            }

            // 其他情况尝试 JSON 转换
            String jsonStr = JsonUtils.toJsonString(params);
            return JsonUtils.parseObject(jsonStr, IotDeviceAuthReqDTO.class);
        } catch (Exception e) {
            log.error("[parseAuthParams][解析认证参数({})失败]", params, e);
            return null;
        }
    }

}
