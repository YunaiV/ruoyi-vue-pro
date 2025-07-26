package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpAuthManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

/**
 * TCP 上行消息处理器
 */
@Slf4j
public class IotTcpUpstreamHandler implements Handler<NetSocket> {

    private static final String CODEC_TYPE_JSON = "TCP_JSON";
    private static final String CODEC_TYPE_BINARY = "TCP_BINARY";
    private static final String AUTH_METHOD = "auth";

    private final IotDeviceMessageService deviceMessageService;

    private final IotDeviceService deviceService;

    private final IotTcpSessionManager sessionManager;

    private final IotTcpAuthManager authManager;

    private final IotDeviceTokenService deviceTokenService;

    private final IotDeviceCommonApi deviceApi;

    private final String serverId;

    public IotTcpUpstreamHandler(IotTcpUpstreamProtocol protocol, IotDeviceMessageService deviceMessageService,
                                 IotDeviceService deviceService, IotTcpSessionManager sessionManager) {
        this.deviceMessageService = deviceMessageService;
        this.deviceService = deviceService;
        this.sessionManager = sessionManager;
        this.authManager = SpringUtil.getBean(IotTcpAuthManager.class);
        this.deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.serverId = protocol.getServerId();
    }

    @Override
    public void handle(NetSocket socket) {
        String clientId = IdUtil.simpleUUID();
        log.info("[handle][收到设备连接] 客户端 ID: {}, 地址: {}", clientId, socket.remoteAddress());

        // 设置异常和关闭处理器
        socket.exceptionHandler(ex -> {
            log.error("[handle][连接异常] 客户端 ID: {}, 地址: {}", clientId, socket.remoteAddress(), ex);
            cleanupSession(socket);
        });

        socket.closeHandler(v -> {
            log.info("[handle][连接关闭] 客户端 ID: {}, 地址: {}", clientId, socket.remoteAddress());
            cleanupSession(socket);
        });

        socket.handler(buffer -> handleDataPackage(clientId, buffer, socket));
    }

    private void handleDataPackage(String clientId, Buffer buffer, NetSocket socket) {
        try {
            if (buffer.length() == 0) {
                log.warn("[handleDataPackage][数据包为空] 客户端 ID: {}", clientId);
                return;
            }

            // 1. 解码消息
            MessageInfo messageInfo = decodeMessage(buffer);
            if (messageInfo == null) {
                return;
            }

            // 2. 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(messageInfo.message.getDeviceId());
            if (device == null) {
                sendError(socket, messageInfo.message.getRequestId(), "设备不存在", messageInfo.codecType);
                return;
            }

            // 3. 处理消息
            if (!authManager.isAuthenticated(socket)) {
                handleAuthRequest(clientId, messageInfo.message, socket, messageInfo.codecType);
            } else {
                IotTcpAuthManager.AuthInfo authInfo = authManager.getAuthInfo(socket);
                handleBusinessMessage(clientId, messageInfo.message, authInfo);
            }
        } catch (Exception e) {
            log.error("[handleDataPackage][处理数据包失败] 客户端 ID: {}, 错误: {}", clientId, e.getMessage(), e);
        }
    }

    /**
     * 处理认证请求
     */
    private void handleAuthRequest(String clientId, IotDeviceMessage message, NetSocket socket, String codecType) {
        try {
            // 1. 验证认证请求
            if (!AUTH_METHOD.equals(message.getMethod())) {
                sendError(socket, message.getRequestId(), "请先进行认证", codecType);
                return;
            }

            // 2. 解析认证参数
            AuthParams authParams = parseAuthParams(message.getParams());
            if (authParams == null) {
                sendError(socket, message.getRequestId(), "认证参数不完整", codecType);
                return;
            }

            // 3. 执行认证流程
            if (performAuthentication(authParams, socket, message.getRequestId(), codecType)) {
                log.info("[handleAuthRequest][认证成功] 客户端 ID: {}, username: {}", clientId, authParams.username);
            }
        } catch (Exception e) {
            log.error("[handleAuthRequest][认证处理异常] 客户端 ID: {}", clientId, e);
            sendError(socket, message.getRequestId(), "认证处理异常: " + e.getMessage(), codecType);
        }
    }

    /**
     * 处理业务消息
     */
    private void handleBusinessMessage(String clientId, IotDeviceMessage message,
                                       IotTcpAuthManager.AuthInfo authInfo) {
        try {
            message.setDeviceId(authInfo.getDeviceId());
            message.setServerId(serverId);

            deviceMessageService.sendDeviceMessage(message, authInfo.getProductKey(), authInfo.getDeviceName(),
                    serverId);
            log.info("[handleBusinessMessage][业务消息处理完成] 客户端 ID: {}, 消息 ID: {}, 设备 ID: {}, 方法: {}",
                    clientId, message.getId(), message.getDeviceId(), message.getMethod());
        } catch (Exception e) {
            log.error("[handleBusinessMessage][处理业务消息失败] 客户端 ID: {}, 错误: {}", clientId, e.getMessage(), e);
        }
    }

    /**
     * 解码消息
     */
    private MessageInfo decodeMessage(Buffer buffer) {
        try {
            String rawData = buffer.toString();
            String codecType = isJsonFormat(rawData) ? CODEC_TYPE_JSON : CODEC_TYPE_BINARY;
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(buffer.getBytes(), codecType);
            return message != null ? new MessageInfo(message, codecType) : null;
        } catch (Exception e) {
            log.debug("[decodeMessage][消息解码失败] 错误: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 执行认证
     */
    private boolean performAuthentication(AuthParams authParams, NetSocket socket, String requestId, String codecType) {
        // 1. 执行认证
        if (!authenticateDevice(authParams)) {
            sendError(socket, requestId, "认证失败", codecType);
            return false;
        }

        // 2. 获取设备信息
        IotDeviceAuthUtils.DeviceInfo deviceInfo = deviceTokenService.parseUsername(authParams.username);
        if (deviceInfo == null) {
            sendError(socket, requestId, "解析设备信息失败", codecType);
            return false;
        }

        IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(),
                deviceInfo.getDeviceName());
        if (device == null) {
            sendError(socket, requestId, "设备不存在", codecType);
            return false;
        }

        // 3. 注册认证信息
        String token = deviceTokenService.createToken(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        registerAuthInfo(socket, device, deviceInfo, token, authParams.clientId);

        // 4. 发送上线消息和成功响应
        IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
        deviceMessageService.sendDeviceMessage(onlineMessage, deviceInfo.getProductKey(), deviceInfo.getDeviceName(),
                serverId);
        sendSuccess(socket, requestId, "认证成功", codecType);

        return true;
    }

    /**
     * 发送响应
     */
    private void sendResponse(NetSocket socket, boolean success, String message, String requestId, String codecType) {
        try {
            Object responseData = buildResponseData(success, message);
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, AUTH_METHOD, responseData,
                    success ? 0 : 401, message);
            byte[] encodedData = deviceMessageService.encodeDeviceMessage(responseMessage, codecType);
            socket.write(Buffer.buffer(encodedData));
            log.debug("[sendResponse][发送响应] success: {}, message: {}, requestId: {}", success, message, requestId);
        } catch (Exception e) {
            log.error("[sendResponse][发送响应失败] requestId: {}", requestId, e);
        }
    }

    /**
     * 构建响应数据（不返回 token）
     */
    private Object buildResponseData(boolean success, String message) {
        return MapUtil.builder()
                .put("success", success)
                .put("message", message)
                .build();
    }

    /**
     * 清理会话
     */
    private void cleanupSession(NetSocket socket) {
        // 如果已认证，发送离线消息
        IotTcpAuthManager.AuthInfo authInfo = authManager.getAuthInfo(socket);
        if (authInfo != null) {
            // 发送离线消息
            IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
            deviceMessageService.sendDeviceMessage(offlineMessage, authInfo.getProductKey(), authInfo.getDeviceName(),
                    serverId);
        }
        sessionManager.unregisterSession(socket);
        authManager.unregisterAuth(socket);
    }

    /**
     * 判断是否为 JSON 格式
     */
    private boolean isJsonFormat(String data) {
        if (StrUtil.isBlank(data))
            return false;
        String trimmed = data.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    /**
     * 解析认证参数
     */
    private AuthParams parseAuthParams(Object params) {
        if (params == null)
            return null;

        JSONObject paramsJson = params instanceof JSONObject ? (JSONObject) params
                : JSONUtil.parseObj(params.toString());
        String clientId = paramsJson.getStr("clientId");
        String username = paramsJson.getStr("username");
        String password = paramsJson.getStr("password");

        return StrUtil.hasBlank(clientId, username, password) ? null : new AuthParams(clientId, username, password);
    }

    /**
     * 认证设备
     */
    private boolean authenticateDevice(AuthParams authParams) {
        CommonResult<Boolean> result = deviceApi
                .authDevice(new cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO()
                        .setClientId(authParams.clientId)
                        .setUsername(authParams.username)
                        .setPassword(authParams.password));
        return result.isSuccess() && result.getData();
    }

    /**
     * 注册认证信息
     */
    private void registerAuthInfo(NetSocket socket, IotDeviceRespDTO device, IotDeviceAuthUtils.DeviceInfo deviceInfo,
                                  String token, String clientId) {
        IotTcpAuthManager.AuthInfo auth = new IotTcpAuthManager.AuthInfo();
        auth.setDeviceId(device.getId());
        auth.setProductKey(deviceInfo.getProductKey());
        auth.setDeviceName(deviceInfo.getDeviceName());
        auth.setToken(token);
        auth.setClientId(clientId);

        authManager.registerAuth(socket, auth);
        sessionManager.registerSession(device.getId(), socket);
    }

    /**
     * 发送错误响应
     */
    private void sendError(NetSocket socket, String requestId, String errorMessage, String codecType) {
        sendResponse(socket, false, errorMessage, requestId, codecType);
    }

    /**
     * 发送成功响应（不返回 token）
     */
    private void sendSuccess(NetSocket socket, String requestId, String message, String codecType) {
        sendResponse(socket, true, message, requestId, codecType);
    }

    /**
     * 认证参数
     */
    private record AuthParams(String clientId, String username, String password) {
    }

    /**
     * 消息信息
     */
    private record MessageInfo(IotDeviceMessage message, String codecType) {
    }
}