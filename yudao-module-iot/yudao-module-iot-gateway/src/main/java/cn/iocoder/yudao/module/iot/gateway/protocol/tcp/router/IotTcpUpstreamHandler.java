package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
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
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

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

    public IotTcpUpstreamHandler(IotTcpUpstreamProtocol protocol,
                                 IotDeviceMessageService deviceMessageService,
                                 IotDeviceService deviceService,
                                 IotTcpConnectionManager connectionManager) {
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

        // 设置消息处理器
        socket.handler(buffer -> {
            try {
                processMessage(clientId, buffer, socket);
            } catch (Exception e) {
                log.error("[handle][消息解码失败，断开连接，客户端 ID: {}，地址: {}，错误: {}]",
                        clientId, socket.remoteAddress(), e.getMessage());
                cleanupConnection(socket);
                socket.close();
            }
        });
    }

    /**
     * 处理消息
     *
     * @param clientId 客户端 ID
     * @param buffer   消息
     * @param socket   网络连接
     * @throws Exception 消息解码失败时抛出异常
     */
    private void processMessage(String clientId, Buffer buffer, NetSocket socket) throws Exception {
        // 1. 基础检查
        if (buffer == null || buffer.length() == 0) {
            return;
        }

        // 2. 获取消息格式类型
        String codecType = getMessageCodecType(buffer, socket);

        // 3. 解码消息
        IotDeviceMessage message;
        try {
            message = deviceMessageService.decodeDeviceMessage(buffer.getBytes(), codecType);
            if (message == null) {
                throw new Exception("解码后消息为空");
            }
        } catch (Exception e) {
            // 消息格式错误时抛出异常，由上层处理连接断开
            throw new Exception("消息解码失败: " + e.getMessage(), e);
        }

        // 4. 根据消息类型路由处理
        try {
            if (AUTH_METHOD.equals(message.getMethod())) {
                // 认证请求
                handleAuthenticationRequest(clientId, message, codecType, socket);
            } else {
                // 业务消息
                handleBusinessRequest(clientId, message, codecType, socket);
            }
        } catch (Exception e) {
            log.error("[processMessage][处理消息失败，客户端 ID: {}，消息方法: {}]",
                    clientId, message.getMethod(), e);
            // 发送错误响应，避免客户端一直等待
            try {
                sendErrorResponse(socket, message.getRequestId(), "消息处理失败", codecType);
            } catch (Exception responseEx) {
                log.error("[processMessage][发送错误响应失败，客户端 ID: {}]", clientId, responseEx);
            }
        }
    }

    /**
     * 处理认证请求
     *
     * @param clientId  客户端 ID
     * @param message   消息信息
     * @param codecType 消息编解码类型
     * @param socket    网络连接
     */
    private void handleAuthenticationRequest(String clientId, IotDeviceMessage message, String codecType,
                                             NetSocket socket) {
        try {
            // 1.1 解析认证参数
            IotDeviceAuthReqDTO authParams = parseAuthParams(message.getParams());
            if (authParams == null) {
                log.warn("[handleAuthenticationRequest][认证参数解析失败，客户端 ID: {}]", clientId);
                sendErrorResponse(socket, message.getRequestId(), "认证参数不完整", codecType);
                return;
            }
            // 1.2 执行认证
            if (!validateDeviceAuth(authParams)) {
                log.warn("[handleAuthenticationRequest][认证失败，客户端 ID: {}，username: {}]",
                        clientId, authParams.getUsername());
                sendErrorResponse(socket, message.getRequestId(), "认证失败", codecType);
                return;
            }

            // 2.1 解析设备信息
            IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(authParams.getUsername());
            if (deviceInfo == null) {
                sendErrorResponse(socket, message.getRequestId(), "解析设备信息失败", codecType);
                return;
            }
            // 2.2 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(),
                    deviceInfo.getDeviceName());
            if (device == null) {
                sendErrorResponse(socket, message.getRequestId(), "设备不存在", codecType);
                return;
            }

            // 3.1 注册连接
            registerConnection(socket, device, clientId, codecType);
            // 3.2 发送上线消息
            sendOnlineMessage(device);
            // 3.3 发送成功响应
            sendSuccessResponse(socket, message.getRequestId(), "认证成功", codecType);
            log.info("[handleAuthenticationRequest][认证成功，设备 ID: {}，设备名: {}]",
                    device.getId(), device.getDeviceName());
        } catch (Exception e) {
            log.error("[handleAuthenticationRequest][认证处理异常，客户端 ID: {}]", clientId, e);
            sendErrorResponse(socket, message.getRequestId(), "认证处理异常", codecType);
        }
    }

    /**
     * 处理业务请求
     *
     * @param clientId  客户端 ID
     * @param message   消息信息
     * @param codecType 消息编解码类型
     * @param socket    网络连接
     */
    private void handleBusinessRequest(String clientId, IotDeviceMessage message, String codecType, NetSocket socket) {
        try {
            // 1. 检查认证状态
            if (connectionManager.isNotAuthenticated(socket)) {
                log.warn("[handleBusinessRequest][设备未认证，客户端 ID: {}]", clientId);
                sendErrorResponse(socket, message.getRequestId(), "请先进行认证", codecType);
                return;
            }

            // 2. 获取认证信息并处理业务消息
            IotTcpConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);

            // 3. 发送消息到消息总线
            deviceMessageService.sendDeviceMessage(message, connectionInfo.getProductKey(),
                    connectionInfo.getDeviceName(), serverId);
            log.info("[handleBusinessRequest][发送消息到消息总线，客户端 ID: {}，消息: {}",
                    clientId, message.toString());
        } catch (Exception e) {
            log.error("[handleBusinessRequest][业务请求处理异常，客户端 ID: {}]", clientId, e);
        }
    }

    /**
     * 获取消息编解码类型
     *
     * @param buffer 消息
     * @param socket 网络连接
     * @return 消息编解码类型
     */
    private String getMessageCodecType(Buffer buffer, NetSocket socket) {
        // 1. 如果已认证，优先使用缓存的编解码类型
        IotTcpConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);
        if (connectionInfo != null && connectionInfo.isAuthenticated() &&
                StrUtil.isNotBlank(connectionInfo.getCodecType())) {
            return connectionInfo.getCodecType();
        }

        // 2. 未认证时检测消息格式类型
        return IotTcpBinaryDeviceMessageCodec.isBinaryFormatQuick(buffer.getBytes()) ? CODEC_TYPE_BINARY
                : CODEC_TYPE_JSON;
    }

    /**
     * 注册连接信息
     *
     * @param socket    网络连接
     * @param device    设备
     * @param clientId  客户端 ID
     * @param codecType 消息编解码类型
     */
    private void registerConnection(NetSocket socket, IotDeviceRespDTO device,
                                    String clientId, String codecType) {
        IotTcpConnectionManager.ConnectionInfo connectionInfo = new IotTcpConnectionManager.ConnectionInfo()
                .setDeviceId(device.getId())
                .setProductKey(device.getProductKey())
                .setDeviceName(device.getDeviceName())
                .setClientId(clientId)
                .setCodecType(codecType)
                .setAuthenticated(true);
        // 注册连接
        connectionManager.registerConnection(socket, device.getId(), connectionInfo);
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
     * 清理连接
     *
     * @param socket 网络连接
     */
    private void cleanupConnection(NetSocket socket) {
        try {
            // 1. 发送离线消息（如果已认证）
            IotTcpConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);
            if (connectionInfo != null) {
                IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
                deviceMessageService.sendDeviceMessage(offlineMessage, connectionInfo.getProductKey(),
                        connectionInfo.getDeviceName(), serverId);
            }

            // 2. 注销连接
            connectionManager.unregisterConnection(socket);
        } catch (Exception e) {
            log.error("[cleanupConnection][清理连接失败]", e);
        }
    }

    /**
     * 发送响应消息
     *
     * @param socket    网络连接
     * @param success   是否成功
     * @param message   消息
     * @param requestId 请求 ID
     * @param codecType 消息编解码类型
     */
    private void sendResponse(NetSocket socket, boolean success, String message, String requestId, String codecType) {
        try {
            Object responseData = MapUtil.builder()
                    .put("success", success)
                    .put("message", message)
                    .build();

            int code = success ? 0 : 401;
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, AUTH_METHOD, responseData,
                    code, message);

            byte[] encodedData = deviceMessageService.encodeDeviceMessage(responseMessage, codecType);
            socket.write(Buffer.buffer(encodedData));

        } catch (Exception e) {
            log.error("[sendResponse][发送响应失败，requestId: {}]", requestId, e);
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
     * 发送错误响应
     *
     * @param socket       网络连接
     * @param requestId    请求 ID
     * @param errorMessage 错误消息
     * @param codecType    消息编解码类型
     */
    private void sendErrorResponse(NetSocket socket, String requestId, String errorMessage, String codecType) {
        sendResponse(socket, false, errorMessage, requestId, codecType);
    }

    /**
     * 发送成功响应
     *
     * @param socket    网络连接
     * @param requestId 请求 ID
     * @param message   消息
     * @param codecType 消息编解码类型
     */
    @SuppressWarnings("SameParameterValue")
    private void sendSuccessResponse(NetSocket socket, String requestId, String message, String codecType) {
        sendResponse(socket, true, message, requestId, codecType);
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
            if (params instanceof java.util.Map) {
                java.util.Map<String, Object> paramMap = (java.util.Map<String, Object>) params;
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