package cn.iocoder.yudao.module.iot.gateway.protocol.websocket.handler.upstream;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.IotWebSocketProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager.IotWebSocketConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


/**
 * WebSocket 上行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotWebSocketUpstreamHandler implements Handler<ServerWebSocket> {

    private static final String AUTH_METHOD = "auth";

    private final String serverId;

    /**
     * 消息序列化器（处理业务消息序列化/反序列化）
     */
    private final IotMessageSerializer serializer;
    /**
     * 连接管理器
     */
    private final IotWebSocketConnectionManager connectionManager;

    // TODO @AI：是不是可以去掉？
    private final boolean binaryPayload;

    private final IotDeviceMessageService deviceMessageService;
    private final IotDeviceService deviceService;
    private final IotDeviceCommonApi deviceApi;

    // TODO @AI：参数、顺序参考 IotTcpUpstreamHandler
    public IotWebSocketUpstreamHandler(IotWebSocketProtocol protocol,
                                       IotDeviceMessageService deviceMessageService,
                                       IotDeviceService deviceService,
                                       IotWebSocketConnectionManager connectionManager,
                                       IotMessageSerializer serializer) {
        this.deviceMessageService = deviceMessageService;
        this.deviceService = deviceService;
        this.connectionManager = connectionManager;
        this.serializer = serializer;
        this.binaryPayload = serializer.getType() == IotSerializeTypeEnum.BINARY;
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.serverId = protocol.getServerId();
        // TODO @AI：通过 springutil；deviceService、deviceMessageService；
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void handle(ServerWebSocket socket) {
        String clientId = IdUtil.simpleUUID();
        log.debug("[handle][设备连接，客户端 ID: {}，地址: {}]", clientId, socket.remoteAddress());

        // 1. 设置异常和关闭处理器
        // TODO @AI：clientId 去掉；
        socket.exceptionHandler(ex -> {
            log.warn("[handle][连接异常，客户端 ID: {}，地址: {}]", clientId, socket.remoteAddress());
            socket.close();
        });
        socket.closeHandler(v -> {
            log.debug("[handle][连接关闭，客户端 ID: {}，地址: {}]", clientId, socket.remoteAddress());
            cleanupConnection(socket);
        });

        // 2. 设置消息处理器（JSON 使用文本，BINARY 使用二进制）
        // TODO @AI：是不是 text、binary 保持统一？用一个 mesagehandler？
        if (binaryPayload) {
            socket.binaryMessageHandler(buffer -> {
                try {
                    processMessage(clientId, buffer.getBytes(), socket);
                } catch (Exception e) {
                    log.error("[handle][消息解码失败，断开连接，客户端 ID: {}，地址: {}，错误: {}]",
                            clientId, socket.remoteAddress(), e.getMessage());
                    cleanupConnection(socket);
                    socket.close();
                }
            });
            socket.textMessageHandler(message -> {
                log.warn("[handle][收到文本帧但当前序列化为 BINARY，断开连接，客户端 ID: {}，地址: {}]",
                        clientId, socket.remoteAddress());
                cleanupConnection(socket);
                socket.close();
            });
        } else {
            socket.textMessageHandler(message -> {
                try {
                    processMessage(clientId, StrUtil.utf8Bytes(message), socket);
                } catch (Exception e) {
                    log.error("[handle][消息解码失败，断开连接，客户端 ID: {}，地址: {}，错误: {}]",
                            clientId, socket.remoteAddress(), e.getMessage());
                    // TODO @AI：是不是不用 cleanupConnection？closehandler 本身就吹了了；
                    cleanupConnection(socket);
                    socket.close();
                }
            });
            socket.binaryMessageHandler(buffer -> {
                try {
                    processMessage(clientId, buffer.getBytes(), socket);
                } catch (Exception e) {
                    log.error("[handle][消息解码失败，断开连接，客户端 ID: {}，地址: {}，错误: {}]",
                            clientId, socket.remoteAddress(), e.getMessage());
                    cleanupConnection(socket);
                    socket.close();
                }
            });
        }
    }

    /**
     * 处理消息
     *
     * @param clientId 客户端 ID
     * @param payload  消息负载
     * @param socket   WebSocket 连接
     * @throws Exception 消息解码失败时抛出异常
     */
    private void processMessage(String clientId, byte[] payload, ServerWebSocket socket) throws Exception {
        // 1.1 基础检查
        if (ArrayUtil.isEmpty(payload)) {
            return;
        }
        // 1.2 解码消息
        IotDeviceMessage deviceMessage;
        try {
            deviceMessage = serializer.deserialize(payload);
            if (deviceMessage == null) {
                throw new Exception("解码后消息为空");
            }
        } catch (Exception e) {
            throw new Exception("消息解码失败: " + e.getMessage(), e);
        }

        // 2. 根据消息类型路由处理
        try {
            if (AUTH_METHOD.equals(deviceMessage.getMethod())) {
                // 认证请求
                handleAuthenticationRequest(clientId, deviceMessage, socket);
            } else if (IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod().equals(deviceMessage.getMethod())) {
                // 设备动态注册请求
                handleRegisterRequest(clientId, deviceMessage, socket);
            } else {
                // 业务消息
                handleBusinessRequest(clientId, deviceMessage, socket);
            }
        } catch (Exception e) {
            // TODO @AI：参考 IotTcpUpstreamHandler 处理；业务、参数、其它
            log.error("[processMessage][处理消息失败，客户端 ID: {}，消息方法: {}]",
                    clientId, deviceMessage.getMethod(), e);
            // 发送错误响应，避免客户端一直等待
            try {
                sendErrorResponse(socket, deviceMessage.getRequestId(), "消息处理失败");
            } catch (Exception responseEx) {
                log.error("[processMessage][发送错误响应失败，客户端 ID: {}]", clientId, responseEx);
            }
        }
    }

    /**
     * 处理认证请求
     *
     * @param clientId 客户端 ID
     * @param message  消息信息
     * @param socket   WebSocket 连接
     */
    private void handleAuthenticationRequest(String clientId, IotDeviceMessage message, ServerWebSocket socket) {
        try {
            // 1.1 解析认证参数
            // TODO @AI：参数解析；参考 tcp 对应的 handleAuthenticationRequest
            IotDeviceAuthReqDTO authParams = parseAuthParams(message.getParams());
            if (authParams == null) {
                log.warn("[handleAuthenticationRequest][认证参数解析失败，客户端 ID: {}]", clientId);
                sendErrorResponse(socket, message.getRequestId(), "认证参数不完整");
                return;
            }
            // 1.2 执行认证
            if (!validateDeviceAuth(authParams)) {
                log.warn("[handleAuthenticationRequest][认证失败，客户端 ID: {}，username: {}]",
                        clientId, authParams.getUsername());
                sendErrorResponse(socket, message.getRequestId(), "认证失败");
                return;
            }

            // 2.1 解析设备信息
            IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(authParams.getUsername());
            if (deviceInfo == null) {
                sendErrorResponse(socket, message.getRequestId(), "解析设备信息失败");
                return;
            }
            // 2.2 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(),
                    deviceInfo.getDeviceName());
            if (device == null) {
                sendErrorResponse(socket, message.getRequestId(), "设备不存在");
                return;
            }

            // 3.1 注册连接
            registerConnection(socket, device, clientId);
            // 3.2 发送上线消息
            sendOnlineMessage(device);
            // 3.3 发送成功响应
            sendSuccessResponse(socket, message.getRequestId(), "认证成功");
            log.info("[handleAuthenticationRequest][认证成功，设备 ID: {}，设备名: {}]",
                    device.getId(), device.getDeviceName());
        } catch (Exception e) {
            log.error("[handleAuthenticationRequest][认证处理异常，客户端 ID: {}]", clientId, e);
            sendErrorResponse(socket, message.getRequestId(), "认证处理异常");
        }
    }

    /**
     * 处理设备动态注册请求（一型一密，不需要认证）
     *
     * @param clientId 客户端 ID
     * @param message  消息信息
     * @param socket   WebSocket 连接
     * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
     */
    private void handleRegisterRequest(String clientId, IotDeviceMessage message, ServerWebSocket socket) {
        // TODO @AI：参数解析；参考 tcp 对应的 handleRegisterRequest
        try {
            // 1. 解析注册参数
            IotDeviceRegisterReqDTO params = parseRegisterParams(message.getParams());
            if (params == null
                    || StrUtil.hasEmpty(params.getProductKey(), params.getDeviceName(), params.getProductSecret())) {
                log.warn("[handleRegisterRequest][注册参数解析失败，客户端 ID: {}]", clientId);
                sendErrorResponse(socket, message.getRequestId(), "注册参数不完整");
                return;
            }

            // 2. 调用动态注册
            CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(params);
            if (result.isError()) {
                log.warn("[handleRegisterRequest][注册失败，客户端 ID: {}，错误: {}]", clientId, result.getMsg());
                sendErrorResponse(socket, message.getRequestId(), result.getMsg());
                return;
            }

            // 3. 发送成功响应（包含 deviceSecret）
            sendRegisterSuccessResponse(socket, message.getRequestId(), result.getData());
            log.info("[handleRegisterRequest][注册成功，客户端 ID: {}，设备名: {}]",
                    clientId, params.getDeviceName());
        } catch (Exception e) {
            log.error("[handleRegisterRequest][注册处理异常，客户端 ID: {}]", clientId, e);
            sendErrorResponse(socket, message.getRequestId(), "注册处理异常");
        }
    }

    // TODO @AI：参考对应的 tcp 的 handleBusinessRequest
    /**
     * 处理业务请求
     *
     * @param clientId 客户端 ID
     * @param message  消息信息
     * @param socket   WebSocket 连接
     */
    private void handleBusinessRequest(String clientId, IotDeviceMessage message, ServerWebSocket socket) {
        try {
            // 1. 获取认证信息并处理业务消息
            IotWebSocketConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);
            if (connectionInfo == null) {
                log.warn("[handleBusinessRequest][连接未认证，拒绝处理业务消息，客户端 ID: {}]", clientId);
                sendErrorResponse(socket, message.getRequestId(), "连接未认证");
                return;
            }

            // 2. 发送消息到消息总线
            deviceMessageService.sendDeviceMessage(message, connectionInfo.getProductKey(),
                    connectionInfo.getDeviceName(), serverId);
            log.info("[handleBusinessRequest][发送消息到消息总线，客户端 ID: {}，消息: {}",
                    clientId, message.toString());
        } catch (Exception e) {
            log.error("[handleBusinessRequest][业务请求处理异常，客户端 ID: {}]", clientId, e);
        }
    }

    /**
     * 注册连接信息
     *
     * @param socket   WebSocket 连接
     * @param device   设备
     * @param clientId 客户端 ID
     */
    private void registerConnection(ServerWebSocket socket, IotDeviceRespDTO device, String clientId) {
        IotWebSocketConnectionManager.ConnectionInfo connectionInfo = new IotWebSocketConnectionManager.ConnectionInfo()
                .setDeviceId(device.getId())
                .setProductKey(device.getProductKey())
                .setDeviceName(device.getDeviceName());
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
     * @param socket WebSocket 连接
     */
    private void cleanupConnection(ServerWebSocket socket) {
        try {
            // 1. 发送离线消息（如果已认证）
            IotWebSocketConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);
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

    // ===================== 发送响应消息 =====================

    /**
     * 发送响应消息
     *
     * @param socket    WebSocket 连接
     * @param success   是否成功
     * @param message   消息
     * @param requestId 请求 ID
     */
    private void sendResponse(ServerWebSocket socket, boolean success, String message, String requestId) {
        try {
            Object responseData = MapUtil.builder()
                    .put("success", success)
                    .put("message", message)
                    .build();

            int code = success ? 0 : 401;
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, AUTH_METHOD, responseData, code, message);

            writeResponse(socket, responseMessage);
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
     * @param socket       WebSocket 连接
     * @param requestId    请求 ID
     * @param errorMessage 错误消息
     */
    private void sendErrorResponse(ServerWebSocket socket, String requestId, String errorMessage) {
        sendResponse(socket, false, errorMessage, requestId);
    }

    /**
     * 发送成功响应
     *
     * @param socket    WebSocket 连接
     * @param requestId 请求 ID
     * @param message   消息
     */
    @SuppressWarnings("SameParameterValue")
    private void sendSuccessResponse(ServerWebSocket socket, String requestId, String message) {
        sendResponse(socket, true, message, requestId);
    }

    /**
     * 解析认证参数
     *
     * @param params 参数对象（通常为 Map 类型）
     * @return 认证参数 DTO，解析失败时返回 null
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
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
            return JsonUtils.convertObject(params, IotDeviceAuthReqDTO.class);
        } catch (Exception e) {
            log.error("[parseAuthParams][解析认证参数({})失败]", params, e);
            return null;
        }
    }

    /**
     * 解析注册参数
     *
     * @param params 参数对象（通常为 Map 类型）
     * @return 注册参数 DTO，解析失败时返回 null
     */
    @SuppressWarnings("unchecked")
    private IotDeviceRegisterReqDTO parseRegisterParams(Object params) {
        if (params == null) {
            return null;
        }
        try {
            // 参数默认为 Map 类型，直接转换
            if (params instanceof Map) {
                Map<String, Object> paramMap = (Map<String, Object>) params;
                return new IotDeviceRegisterReqDTO()
                        .setProductKey(MapUtil.getStr(paramMap, "productKey"))
                        .setDeviceName(MapUtil.getStr(paramMap, "deviceName"))
                        .setProductSecret(MapUtil.getStr(paramMap, "productSecret"));
            }
            // 如果已经是目标类型，直接返回
            if (params instanceof IotDeviceRegisterReqDTO) {
                return (IotDeviceRegisterReqDTO) params;
            }

            // 其他情况尝试 JSON 转换
            return JsonUtils.convertObject(params, IotDeviceRegisterReqDTO.class);
        } catch (Exception e) {
            log.error("[parseRegisterParams][解析注册参数({})失败]", params, e);
            return null;
        }
    }

    /**
     * 发送注册成功响应（包含 deviceSecret）
     *
     * @param socket       WebSocket 连接
     * @param requestId    请求 ID
     * @param registerResp 注册响应
     */
    private void sendRegisterSuccessResponse(ServerWebSocket socket, String requestId,
                                             IotDeviceRegisterRespDTO registerResp) {
        try {
            // 1. 构建响应消息（参考 HTTP 返回格式，直接返回 IotDeviceRegisterRespDTO）
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId,
                    IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), registerResp, 0, null);
            // 2. 发送响应
            writeResponse(socket, responseMessage);
        } catch (Exception e) {
            log.error("[sendRegisterSuccessResponse][发送注册成功响应失败，requestId: {}]", requestId, e);
        }
    }

    /**
     * 写入响应消息
     */
    private void writeResponse(ServerWebSocket socket, IotDeviceMessage responseMessage) {
        byte[] payload = serializer.serialize(responseMessage);
        if (binaryPayload) {
            socket.writeBinaryMessage(Buffer.buffer(payload));
        } else {
            socket.writeTextMessage(StrUtil.utf8Str(payload));
        }
    }

}
