package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpBinaryDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpJsonDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * TCP 上行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpUpstreamHandler implements Handler<NetSocket> {

    private static final String CODEC_TYPE_JSON = IotTcpJsonDeviceMessageCodec.TYPE;
    private static final String CODEC_TYPE_BINARY = IotTcpBinaryDeviceMessageCodec.TYPE;
    private static final String AUTH_METHOD = "auth";

    private final IotDeviceMessageService deviceMessageService;

    private final IotDeviceService deviceService;

    private final IotTcpConnectionManager connectionManager;

    private final IotDeviceCommonApi deviceApi;

    private final String serverId;

    public IotTcpUpstreamHandler(IotTcpUpstreamProtocol protocol, IotDeviceMessageService deviceMessageService,
            IotDeviceService deviceService, IotTcpConnectionManager connectionManager) {
        this.deviceMessageService = deviceMessageService;
        this.deviceService = deviceService;
        this.connectionManager = connectionManager;
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.serverId = protocol.getServerId();
    }

    @Override
    public void handle(NetSocket socket) {
        String clientId = IdUtil.simpleUUID();
        log.debug("[handle][设备连接，客户端 ID: {}，地址: {}]", clientId, socket.remoteAddress());

        // 设置异常和关闭处理器
        socket.exceptionHandler(ex -> {
            log.warn("[handle][连接异常，客户端 ID: {}，地址: {}]", clientId, socket.remoteAddress());
            cleanupConnection(socket);
        });

        socket.closeHandler(v -> {
            log.debug("[handle][连接关闭，客户端 ID: {}，地址: {}]", clientId, socket.remoteAddress());
            cleanupConnection(socket);
        });

        socket.handler(buffer -> processMessage(clientId, buffer, socket));
    }

    /**
     * 处理消息
     */
    private void processMessage(String clientId, Buffer buffer, NetSocket socket) {
        try {
            // 1. 数据包基础检查
            if (buffer.length() == 0) {
                return;
            }

            // 2. 解码消息
            MessageInfo messageInfo = decodeMessage(buffer);
            if (messageInfo == null) {
                return;
            }

            // 3. 根据消息类型路由处理
            if (isAuthRequest(messageInfo.message)) {
                // 认证请求：无需检查认证状态
                handleAuthenticationRequest(clientId, messageInfo, socket);
            } else {
                // 业务消息：需要检查认证状态
                handleBusinessRequest(clientId, messageInfo, socket);
            }

        } catch (Exception e) {
            log.error("[processMessage][处理消息失败，客户端 ID: {}]", clientId, e);
        }
    }

    /**
     * 处理认证请求
     */
    private void handleAuthenticationRequest(String clientId, MessageInfo messageInfo, NetSocket socket) {
        try {
            IotDeviceMessage message = messageInfo.message;

            // 1. 解析认证参数
            AuthParams authParams = parseAuthParams(message.getParams());
            if (authParams == null) {
                sendError(socket, message.getRequestId(), "认证参数不完整", messageInfo.codecType);
                return;
            }

            // 2. 执行认证
            if (!authenticateDevice(authParams)) {
                log.warn("[handleAuthenticationRequest][认证失败，客户端 ID: {}，username: {}]",
                        clientId, authParams.username);
                sendError(socket, message.getRequestId(), "认证失败", messageInfo.codecType);
                return;
            }

            // 3. 解析设备信息
            IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(authParams.username);
            if (deviceInfo == null) {
                sendError(socket, message.getRequestId(), "解析设备信息失败", messageInfo.codecType);
                return;
            }

            // 4. 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(),
                    deviceInfo.getDeviceName());
            if (device == null) {
                sendError(socket, message.getRequestId(), "设备不存在", messageInfo.codecType);
                return;
            }

            // 5. 注册连接并发送成功响应
            registerConnection(socket, device, deviceInfo, authParams.clientId);
            sendOnlineMessage(deviceInfo);
            sendSuccess(socket, message.getRequestId(), "认证成功", messageInfo.codecType);

            log.info("[handleAuthenticationRequest][认证成功，设备 ID: {}，设备名: {}]",
                    device.getId(), deviceInfo.getDeviceName());

        } catch (Exception e) {
            log.error("[handleAuthenticationRequest][认证处理异常，客户端 ID: {}]", clientId, e);
            sendError(socket, messageInfo.message.getRequestId(), "认证处理异常", messageInfo.codecType);
        }
    }

    /**
     * 处理业务请求
     */
    private void handleBusinessRequest(String clientId, MessageInfo messageInfo, NetSocket socket) {
        try {
            // 1. 检查认证状态
            if (connectionManager.isNotAuthenticated(socket)) {
                log.warn("[handleBusinessRequest][设备未认证，客户端 ID: {}]", clientId);
                sendError(socket, messageInfo.message.getRequestId(), "请先进行认证", messageInfo.codecType);
                return;
            }

            // 2. 获取认证信息并处理业务消息
            IotTcpConnectionManager.AuthInfo authInfo = connectionManager.getAuthInfo(socket);
            processBusinessMessage(clientId, messageInfo.message, authInfo);

        } catch (Exception e) {
            log.error("[handleBusinessRequest][业务请求处理异常，客户端 ID: {}]", clientId, e);
        }
    }

    /**
     * 处理业务消息
     */
    private void processBusinessMessage(String clientId, IotDeviceMessage message,
            IotTcpConnectionManager.AuthInfo authInfo) {
        try {
            message.setDeviceId(authInfo.getDeviceId());
            message.setServerId(serverId);

            // 发送到消息总线
            deviceMessageService.sendDeviceMessage(message, authInfo.getProductKey(),
                    authInfo.getDeviceName(), serverId);

        } catch (Exception e) {
            log.error("[processBusinessMessage][业务消息处理失败，客户端 ID: {}，消息 ID: {}]",
                    clientId, message.getId(), e);
        }
    }

    /**
     * 解码消息
     */
    private MessageInfo decodeMessage(Buffer buffer) {
        if (buffer == null || buffer.length() == 0) {
            return null;
        }

        // 1. 快速检测消息格式类型
        String codecType = detectMessageFormat(buffer);

        try {
            // 2. 使用检测到的格式进行解码
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(buffer.getBytes(), codecType);

            if (message == null) {
                return null;
            }

            return new MessageInfo(message, codecType);

        } catch (Exception e) {
            log.warn("[decodeMessage][消息解码失败，格式: {}，数据长度: {}，错误: {}]",
                    codecType, buffer.length(), e.getMessage());
            return null;
        }
    }

    /**
     * 检测消息格式类型
     * 优化性能：避免不必要的字符串转换
     */
    private String detectMessageFormat(Buffer buffer) {
        if (buffer.length() == 0) {
            return CODEC_TYPE_JSON; // 默认使用 JSON
        }

        // 1. 优先检测二进制格式（检查魔术字节 0x7E）
        if (isBinaryFormat(buffer)) {
            return CODEC_TYPE_BINARY;
        }

        // 2. 检测 JSON 格式（检查前几个有效字符）
        if (isJsonFormat(buffer)) {
            return CODEC_TYPE_JSON;
        }

        // 3. 默认尝试 JSON 格式
        return CODEC_TYPE_JSON;
    }

    /**
     * 检测二进制格式
     * 通过检查魔术字节快速识别，避免完整字符串转换
     */
    private boolean isBinaryFormat(Buffer buffer) {
        // 二进制协议最小长度检查
        if (buffer.length() < 8) {
            return false;
        }

        try {
            // 检查魔术字节 0x7E（二进制协议的第一个字节）
            byte firstByte = buffer.getByte(0);
            return firstByte == (byte) 0x7E;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检测 JSON 格式
     * 只检查前几个有效字符，避免完整字符串转换
     */
    private boolean isJsonFormat(Buffer buffer) {
        try {
            // 检查前 64 个字节或整个缓冲区（取较小值）
            int checkLength = Math.min(buffer.length(), 64);
            String prefix = buffer.getString(0, checkLength, StandardCharsets.UTF_8.name());

            if (StrUtil.isBlank(prefix)) {
                return false;
            }

            String trimmed = prefix.trim();
            // JSON 格式必须以 { 或 [ 开头
            return trimmed.startsWith("{") || trimmed.startsWith("[");

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 注册连接信息
     */
    private void registerConnection(NetSocket socket, IotDeviceRespDTO device,
            IotDeviceAuthUtils.DeviceInfo deviceInfo, String clientId) {
        // 创建认证信息
        IotTcpConnectionManager.AuthInfo authInfo = new IotTcpConnectionManager.AuthInfo()
                .setDeviceId(device.getId())
                .setProductKey(deviceInfo.getProductKey())
                .setDeviceName(deviceInfo.getDeviceName())
                .setClientId(clientId);

        // 注册连接
        connectionManager.registerConnection(socket, device.getId(), authInfo);
    }

    /**
     * 发送设备上线消息
     */
    private void sendOnlineMessage(IotDeviceAuthUtils.DeviceInfo deviceInfo) {
        try {
            IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
            deviceMessageService.sendDeviceMessage(onlineMessage, deviceInfo.getProductKey(),
                    deviceInfo.getDeviceName(), serverId);
        } catch (Exception e) {
            log.error("[sendOnlineMessage][发送上线消息失败，设备: {}]", deviceInfo.getDeviceName(), e);
        }
    }

    /**
     * 清理连接
     */
    private void cleanupConnection(NetSocket socket) {
        try {
            // 发送离线消息（如果已认证）
            IotTcpConnectionManager.AuthInfo authInfo = connectionManager.getAuthInfo(socket);
            if (authInfo != null) {
                IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
                deviceMessageService.sendDeviceMessage(offlineMessage, authInfo.getProductKey(),
                        authInfo.getDeviceName(), serverId);
            }

            // 注销连接
            connectionManager.unregisterConnection(socket);
        } catch (Exception e) {
            log.error("[cleanupConnection][清理连接失败]", e);
        }
    }

    /**
     * 发送响应消息
     */
    private void sendResponse(NetSocket socket, boolean success, String message, String requestId, String codecType) {
        try {
            Object responseData = MapUtil.builder()
                    .put("success", success)
                    .put("message", message)
                    .build();

            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, AUTH_METHOD, responseData,
                    success ? 0 : 401, message);

            byte[] encodedData = deviceMessageService.encodeDeviceMessage(responseMessage, codecType);
            socket.write(Buffer.buffer(encodedData));

        } catch (Exception e) {
            log.error("[sendResponse][发送响应失败，requestId: {}]", requestId, e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断是否为认证请求
     */
    private boolean isAuthRequest(IotDeviceMessage message) {
        return AUTH_METHOD.equals(message.getMethod());
    }

    /**
     * 解析认证参数
     */
    private AuthParams parseAuthParams(Object params) {
        if (params == null) {
            return null;
        }

        try {
            JSONObject paramsJson = params instanceof JSONObject ? (JSONObject) params
                    : JSONUtil.parseObj(params.toString());

            String clientId = paramsJson.getStr("clientId");
            String username = paramsJson.getStr("username");
            String password = paramsJson.getStr("password");

            return StrUtil.hasBlank(clientId, username, password) ? null
                    : new AuthParams(clientId, username, password);
        } catch (Exception e) {
            log.warn("[parseAuthParams][解析认证参数失败]", e);
            return null;
        }
    }

    /**
     * 认证设备
     */
    private boolean authenticateDevice(AuthParams authParams) {
        try {
            CommonResult<Boolean> result = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                    .setClientId(authParams.clientId)
                    .setUsername(authParams.username)
                    .setPassword(authParams.password));
            return result.isSuccess() && Boolean.TRUE.equals(result.getData());
        } catch (Exception e) {
            log.error("[authenticateDevice][设备认证异常，username: {}]", authParams.username, e);
            return false;
        }
    }

    /**
     * 发送错误响应
     */
    private void sendError(NetSocket socket, String requestId, String errorMessage, String codecType) {
        sendResponse(socket, false, errorMessage, requestId, codecType);
    }

    /**
     * 发送成功响应
     */
    private void sendSuccess(NetSocket socket, String requestId, String message, String codecType) {
        sendResponse(socket, true, message, requestId, codecType);
    }

    // ==================== 内部类 ====================

    /**
     * 认证参数
     */
    @Data
    @AllArgsConstructor
    private static class AuthParams {
        private final String clientId;
        private final String username;
        private final String password;
    }

    /**
     * 消息信息
     */
    @Data
    @AllArgsConstructor
    private static class MessageInfo {
        private final IotDeviceMessage message;
        private final String codecType;
    }
}