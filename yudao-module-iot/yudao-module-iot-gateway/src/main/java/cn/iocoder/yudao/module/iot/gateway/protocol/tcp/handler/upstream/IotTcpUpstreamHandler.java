package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.handler.upstream;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
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
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * TCP 上行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpUpstreamHandler implements Handler<NetSocket> {

    private static final String AUTH_METHOD = "auth";

    private final IotDeviceMessageService deviceMessageService;

    private final IotDeviceService deviceService;

    private final IotTcpConnectionManager connectionManager;

    private final IotDeviceCommonApi deviceApi;

    private final String serverId;

    /**
     * TCP 帧编解码器（处理粘包/拆包）
     */
    private final IotTcpFrameCodec codec;
    /**
     * 消息序列化器（处理业务消息序列化/反序列化）
     */
    private final IotMessageSerializer serializer;

    public IotTcpUpstreamHandler(IotProtocol protocol,
                                 IotDeviceMessageService deviceMessageService,
                                 IotDeviceService deviceService,
                                 IotTcpConnectionManager connectionManager,
                                 IotTcpFrameCodec codec,
                                 IotMessageSerializer serializer) {
        this.serverId = protocol.getServerId();
        this.codec = codec;
        this.serializer = serializer;
        this.connectionManager = connectionManager;
        // TODO @AI：都通过 springutil 获取下；
        this.deviceMessageService = deviceMessageService;
        this.deviceService = deviceService;
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
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

        // 设置消息处理器（带拆包支持）
        Handler<Buffer> messageHandler = buffer -> {
            // TODO @AI：需要跟 AI 讨论。哪些情况关闭；哪些情况，发送异常消息；
            try {
                processMessage(clientId, buffer, socket);
            } catch (Exception e) {
                // TODO @AI：这里能合并到 exceptionHandler 么？还是怎么搞好点；
                log.error("[handle][消息解码失败，断开连接，客户端 ID: {}，地址: {}，错误: {}]",
                        clientId, socket.remoteAddress(), e.getMessage());
                cleanupConnection(socket);
                socket.close();
            }
        };

        // 根据是否配置了 FrameCodec 来决定是否使用拆包器
        // TODO @AI：必须配置！
        if (codec != null) {
            // 使用拆包器处理粘包/拆包
            RecordParser parser = codec.createDecodeParser(messageHandler);
            socket.handler(parser);
            log.debug("[handle][启用 {} 拆包器，客户端 ID: {}]", codec.getType(), clientId);
        } else {
            // 未配置拆包器，直接处理原始数据（可能存在粘包问题）
            socket.handler(messageHandler);
            log.debug("[handle][未配置拆包器，客户端 ID: {}]", clientId);
        }
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
        // TODO @AI：不太应该为空？！
        if (buffer == null || buffer.length() == 0) {
            return;
        }

        // 2. 反序列化消息
        IotDeviceMessage message;
        try {
            message = serializer.deserialize(buffer.getBytes());
            if (message == null) {
                throw new IllegalArgumentException("反序列化后消息为空");
            }
        } catch (Exception e) {
            // TODO @AI：是不是不用 try catch？
            throw new Exception("消息反序列化失败: " + e.getMessage(), e);
        }

        // 3. 根据消息类型路由处理
        try {
            if (AUTH_METHOD.equals(message.getMethod())) {
                // 认证请求
                handleAuthenticationRequest(clientId, message, socket);
            } else if (IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod().equals(message.getMethod())) {
                // 设备动态注册请求
                handleRegisterRequest(clientId, message, socket);
            } else {
                // 业务消息
                handleBusinessRequest(clientId, message, socket);
            }
        } catch (Exception e) {
            // TODO @AI：如果参数不正确，不断开连接；
            log.error("[processMessage][处理消息失败，客户端 ID: {}，消息方法: {}]", clientId, message.getMethod(), e);
            // 发送错误响应，避免客户端一直等待
            // TODO @AI：发送失败，是不是不用 try catch？
            try {
                sendErrorResponse(socket, message.getRequestId(), "消息处理失败");
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
     * @param socket   网络连接
     */
    private void handleAuthenticationRequest(String clientId, IotDeviceMessage message, NetSocket socket) {
        try {
            // 1.1 解析认证参数
            // TODO @AI：直接 JsonUtils.convertObject(params, IotDeviceAuthReqDTO.class)；然后，校验参数，不正确抛出 invalid exception；和 http 那一样；
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
            // TODO @AI：这里就断言 deviceInfo 不为空了？！
            if (deviceInfo == null) {
                sendErrorResponse(socket, message.getRequestId(), "解析设备信息失败");
                return;
            }
            // 2.2 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            // TODO @AI：这里就断言 device 不为空了？！
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
            // TODO @AI：最大化去掉 try catch；（这个方法里的）
            log.error("[handleAuthenticationRequest][认证处理异常，客户端 ID: {}]", clientId, e);
            sendErrorResponse(socket, message.getRequestId(), "认证处理异常");
        }
    }

    /**
     * 处理设备动态注册请求（一型一密，不需要认证）
     *
     * @param clientId 客户端 ID
     * @param message  消息信息
     * @param socket   网络连接
     * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
     */
    private void handleRegisterRequest(String clientId, IotDeviceMessage message, NetSocket socket) {
        try {
            // 1. 解析注册参数
            IotDeviceRegisterReqDTO params = parseRegisterParams(message.getParams());
            if (params == null) {
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

    /**
     * 处理业务请求
     *
     * @param clientId 客户端 ID
     * @param message  消息信息
     * @param socket   网络连接
     */
    private void handleBusinessRequest(String clientId, IotDeviceMessage message, NetSocket socket) {
        try {
            // 1. 检查认证状态
            if (connectionManager.isNotAuthenticated(socket)) {
                log.warn("[handleBusinessRequest][设备未认证，客户端 ID: {}]", clientId);
                sendErrorResponse(socket, message.getRequestId(), "请先进行认证");
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
     * 注册连接信息
     *
     * @param socket   网络连接
     * @param device   设备
     * @param clientId 客户端 ID
     */
    private void registerConnection(NetSocket socket, IotDeviceRespDTO device, String clientId) {
        IotTcpConnectionManager.ConnectionInfo connectionInfo = new IotTcpConnectionManager.ConnectionInfo()
                .setDeviceId(device.getId())
                .setProductKey(device.getProductKey())
                .setDeviceName(device.getDeviceName())
                .setClientId(clientId);
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
     */
    private void sendResponse(NetSocket socket, boolean success, String message, String requestId) {
        try {
            // TODO @AI：是不是不用
            Object responseData = MapUtil.builder()
                    .put("success", success)
                    .put("message", message)
                    .build();

            int code = success ? 0 : 401;
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, AUTH_METHOD, responseData,
                    code, message);

            // 序列化 + 帧编码
            byte[] serializedData = serializer.serialize(responseMessage);
            Buffer frameData = codec.encode(serializedData);
            socket.write(frameData);

        } catch (Exception e) {
            log.error("[sendResponse][发送响应失败，requestId: {}]", requestId, e);
        }
    }

    // TODO @AI：合并到 handleAuthenticationRequest 里；
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
     */
    private void sendErrorResponse(NetSocket socket, String requestId, String errorMessage) {
        sendResponse(socket, false, errorMessage, requestId);
    }

    /**
     * 发送成功响应
     *
     * @param socket    网络连接
     * @param requestId 请求 ID
     * @param message   消息
     */
    @SuppressWarnings("SameParameterValue")
    private void sendSuccessResponse(NetSocket socket, String requestId, String message) {
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
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
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
            String jsonStr = JsonUtils.toJsonString(params);
            return JsonUtils.parseObject(jsonStr, IotDeviceRegisterReqDTO.class);
        } catch (Exception e) {
            log.error("[parseRegisterParams][解析注册参数({})失败]", params, e);
            return null;
        }
    }

    /**
     * 发送注册成功响应（包含 deviceSecret）
     *
     * @param socket       网络连接
     * @param requestId    请求 ID
     * @param registerResp 注册响应
     */
    private void sendRegisterSuccessResponse(NetSocket socket, String requestId,
                                             IotDeviceRegisterRespDTO registerResp) {
        try {
            // 1. 构建响应消息（参考 HTTP 返回格式，直接返回 IotDeviceRegisterRespDTO）
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId,
                    IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), registerResp, 0, null);
            // 2. 序列化 + 帧编码
            byte[] serializedData = serializer.serialize(responseMessage);
            Buffer frameData = codec.encode(serializedData);
            socket.write(frameData);
        } catch (Exception e) {
            log.error("[sendRegisterSuccessResponse][发送注册成功响应失败，requestId: {}]", requestId, e);
        }
    }

}