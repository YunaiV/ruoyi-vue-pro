package cn.iocoder.yudao.module.iot.gateway.protocol.websocket.handler.upstream;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
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
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager.IotWebSocketConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.lang.Assert;

import java.util.concurrent.RejectedExecutionException;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_AUTH_FAIL;


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

    private final IotDeviceMessageService deviceMessageService;
    private final IotDeviceService deviceService;
    private final IotDeviceCommonApi deviceApi;

    public IotWebSocketUpstreamHandler(String serverId,
                                       IotMessageSerializer serializer,
                                       IotWebSocketConnectionManager connectionManager) {
        this.serverId = serverId;
        this.serializer = serializer;
        this.connectionManager = connectionManager;
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.deviceService = SpringUtil.getBean(IotDeviceService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void handle(ServerWebSocket socket) {
        String remoteAddress = String.valueOf(socket.remoteAddress());
        log.debug("[handle][设备连接，地址: {}]", remoteAddress);

        // 1. 设置异常和关闭处理器
        socket.exceptionHandler(ex -> {
            log.warn("[handle][连接异常，地址: {}]", remoteAddress, ex);
            socket.close();
        });
        // 本连接的 worker 队列：业务消息（text 帧）、透传帧（binary 帧）的发送会阻塞，按收到的顺序在 worker 上执行；离线消息排在它们之后
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(Vertx.currentContext());
        socket.closeHandler(v -> {
            log.debug("[handle][连接关闭，地址: {}]", remoteAddress);
            cleanupConnection(socket, queue);
        });

        // 2. 设置 text 帧处理器：认证、注册、标准业务消息
        socket.textMessageHandler(message -> {
            try {
                processMessage(StrUtil.utf8Bytes(message), socket, queue);
            } catch (Exception e) {
                log.error("[handle][消息解码失败，断开连接，地址: {}，错误: {}]", remoteAddress, e.getMessage());
                socket.close();
            }
        });
        // 3. 设置 binary 帧处理器：透传上报，必须先通过 text 帧认证
        socket.binaryMessageHandler(buffer -> {
            try {
                processRawMessage(buffer.getBytes(), socket, remoteAddress, queue);
            } catch (Exception e) {
                log.error("[handle][透传消息处理失败，断开连接，地址: {}，错误: {}]", remoteAddress, e.getMessage());
                socket.close();
            }
        });
    }

    /**
     * 处理消息
     *
     * @param payload  消息负载
     * @param socket   WebSocket 连接
     * @param queue    本连接的 worker 队列
     */
    private void processMessage(byte[] payload, ServerWebSocket socket, IotWorkerTaskQueue queue) {
        IotDeviceMessage message = null;
        try {
            // 1.1 基础检查
            if (ArrayUtil.isEmpty(payload)) {
                return;
            }
            // 1.2 解码消息
            message = serializer.deserialize(payload);
            Assert.notNull(message, "消息反序列化失败");
            Assert.notBlank(message.getMethod(), "method 不能为空");

            // 2. 根据消息类型路由处理
            if (AUTH_METHOD.equals(message.getMethod())) {
                handleAuthenticationRequest(message, socket);
            } else if (IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod().equals(message.getMethod())) {
                handleRegisterRequest(message, socket);
            } else {
                handleBusinessRequest(message, socket, queue);
            }
        } catch (ServiceException e) {
            log.warn("[processMessage][业务异常，错误: {}]", e.getMessage());
            String requestId = message != null ? message.getRequestId() : null;
            String method = message != null ? message.getMethod() : null;
            sendErrorResponse(socket, requestId, method, e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[processMessage][参数校验失败，错误: {}]", e.getMessage());
            String requestId = message != null ? message.getRequestId() : null;
            String method = message != null ? message.getMethod() : null;
            sendErrorResponse(socket, requestId, method, BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[processMessage][处理消息失败]", e);
            String requestId = message != null ? message.getRequestId() : null;
            String method = message != null ? message.getMethod() : null;
            sendErrorResponse(socket, requestId, method, INTERNAL_SERVER_ERROR.getCode(),
                    INTERNAL_SERVER_ERROR.getMsg());
            throw e;
        }
    }

    /**
     * 处理认证请求
     *
     * @param message  消息信息
     * @param socket   WebSocket 连接
     */
    @SuppressWarnings("DuplicatedCode")
    private void handleAuthenticationRequest(IotDeviceMessage message, ServerWebSocket socket) {
        // 1. 解析认证参数
        IotDeviceAuthReqDTO authParams = JsonUtils.convertObject(message.getParams(), IotDeviceAuthReqDTO.class);
        Assert.notNull(authParams, "认证参数不能为空");
        Assert.notBlank(authParams.getUsername(), "username 不能为空");
        Assert.notBlank(authParams.getPassword(), "password 不能为空");

        // 2.1 执行认证
        CommonResult<Boolean> authResult = deviceApi.authDevice(authParams);
        authResult.checkError();
        if (BooleanUtil.isFalse(authResult.getData())) {
            throw exception(DEVICE_AUTH_FAIL);
        }
        // 2.2 解析设备信息
        IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(authParams.getUsername());
        Assert.notNull(deviceInfo, "解析设备信息失败");
        // 2.3 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        Assert.notNull(device, "设备不存在");

        // 3.1 注册连接
        registerConnection(socket, device);
        // 3.2 发送上线消息
        sendOnlineMessage(device);
        // 3.3 发送成功响应
        sendSuccessResponse(socket, message.getRequestId(), AUTH_METHOD, "认证成功");
        log.info("[handleAuthenticationRequest][认证成功，设备 ID: {}，设备名: {}]", device.getId(), device.getDeviceName());
    }

    /**
     * 处理设备动态注册请求（一型一密，不需要认证）
     *
     * @param message  消息信息
     * @param socket   WebSocket 连接
     * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
     */
    @SuppressWarnings("DuplicatedCode")
    private void handleRegisterRequest(IotDeviceMessage message, ServerWebSocket socket) {
        // 1. 解析注册参数
        IotDeviceRegisterReqDTO params = JsonUtils.convertObject(message.getParams(), IotDeviceRegisterReqDTO.class);
        Assert.notNull(params, "注册参数不能为空");
        Assert.notBlank(params.getProductKey(), "productKey 不能为空");
        Assert.notBlank(params.getDeviceName(), "deviceName 不能为空");
        Assert.notBlank(params.getSign(), "sign 不能为空");

        // 2. 调用动态注册
        CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(params);
        result.checkError();

        // 3. 发送成功响应（包含 deviceSecret）
        sendSuccessResponse(socket, message.getRequestId(),
                IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), result.getData());
        log.info("[handleRegisterRequest][注册成功，设备名: {}]", params.getDeviceName());
    }

    /**
     * 处理业务请求：认证检查在 event loop 上；获取设备、发送总线会阻塞，交给本连接的 worker 队列按收到的顺序执行
     *
     * @param message  消息信息
     * @param socket   WebSocket 连接
     * @param queue    本连接的 worker 队列
     */
    private void handleBusinessRequest(IotDeviceMessage message, ServerWebSocket socket, IotWorkerTaskQueue queue) {
        // 1. 获取认证信息并处理业务消息
        IotWebSocketConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);
        if (connectionInfo == null) {
            log.warn("[handleBusinessRequest][连接未认证，拒绝处理业务消息]");
            sendErrorResponse(socket, message.getRequestId(), message.getMethod(),
                    UNAUTHORIZED.getCode(), "设备未认证，无法处理业务消息");
            return;
        }

        // 2. 发送消息到消息总线（worker）：成功不应答（与原来一致）；失败回到 event loop 应答错误
        queue.submit(() -> {
            deviceMessageService.sendDeviceMessage(message, connectionInfo.getProductKey(),
                    connectionInfo.getDeviceName(), serverId);
            return null;
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                log.info("[handleBusinessRequest][发送消息到消息总线，消息: {}]", message);
                return;
            }
            handleBusinessFailure(message, socket, ar.cause());
        });
    }

    /**
     * 业务消息在 worker 上处理失败（event loop 上执行）：与同步处理时的异常应答一致
     *
     * @param message 消息信息
     * @param socket  WebSocket 连接
     * @param e       异常
     */
    private void handleBusinessFailure(IotDeviceMessage message, ServerWebSocket socket, Throwable e) {
        if (e instanceof RejectedExecutionException) { // 队列已满：断开连接，由设备重连
            log.error("[handleBusinessFailure][业务消息积压，断开连接，地址: {}，错误: {}]", socket.remoteAddress(), e.getMessage());
            socket.close();
            return;
        }
        if (e instanceof ServiceException) {
            log.warn("[handleBusinessFailure][业务异常，错误: {}]", e.getMessage());
            sendErrorResponse(socket, message.getRequestId(), message.getMethod(),
                    ((ServiceException) e).getCode(), e.getMessage());
            return;
        }
        if (e instanceof IllegalArgumentException) {
            log.warn("[handleBusinessFailure][参数校验失败，错误: {}]", e.getMessage());
            sendErrorResponse(socket, message.getRequestId(), message.getMethod(), BAD_REQUEST.getCode(), e.getMessage());
            return;
        }
        // 其他异常：返回 500，并断开连接
        log.error("[handleBusinessFailure][处理消息失败]", e);
        sendErrorResponse(socket, message.getRequestId(), message.getMethod(), INTERNAL_SERVER_ERROR.getCode(),
                INTERNAL_SERVER_ERROR.getMsg());
        socket.close();
    }

    /**
     * 处理透传上报（binary 帧）：原始字节按产品的数据解析脚本 decode 后发送；会阻塞，交给本连接的 worker 队列按顺序执行
     *
     * @param payload       原始字节
     * @param socket        WebSocket 连接
     * @param remoteAddress 远程地址
     * @param queue         本连接的 worker 队列
     */
    private void processRawMessage(byte[] payload, ServerWebSocket socket, String remoteAddress,
                                   IotWorkerTaskQueue queue) {
        // 1. 未认证的 binary 帧：断开连接
        IotWebSocketConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);
        if (connectionInfo == null) {
            log.warn("[processRawMessage][未认证的 binary 帧，断开连接，地址: {}]", remoteAddress);
            socket.close();
            return;
        }

        // 2. 交给本连接的 worker 队列：截止时间从收到（入队之前）开始计算
        long receivedNanos = System.nanoTime();
        queue.submit(() -> deviceMessageService.sendRawMessage(payload, connectionInfo.getProductKey(),
                connectionInfo.getDeviceName(), serverId, IotProtocolTypeEnum.WEBSOCKET, receivedNanos)).onFailure(e -> {
            if (e instanceof RejectedExecutionException) { // 队列已满：断开连接，由设备重连
                log.error("[processRawMessage][透传上报积压，断开连接，地址: {}，错误: {}]", remoteAddress, e.getMessage());
                socket.close();
                return;
            }
            log.warn("[processRawMessage][透传上报处理失败，地址: {}，数据长度: {} 字节，错误: {}]",
                    remoteAddress, payload.length, e.getMessage());
        });
    }

    /**
     * 注册连接信息
     *
     * @param socket   WebSocket 连接
     * @param device   设备
     */
    private void registerConnection(ServerWebSocket socket, IotDeviceRespDTO device) {
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
     * @param queue  本连接的 worker 队列
     */
    private void cleanupConnection(ServerWebSocket socket, IotWorkerTaskQueue queue) {
        // 1. 注销连接
        IotWebSocketConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);
        connectionManager.unregisterConnection(socket);
        if (connectionInfo == null) {
            return;
        }

        // 2. 发送离线消息（如果已认证）：排在本连接已收到的业务消息、透传帧之后；执行时设备已在新连接上线，则不再发送
        queue.submitLast(() -> {
            if (connectionManager.getConnectionInfoByDeviceId(connectionInfo.getDeviceId()) != null) {
                return null;
            }
            deviceMessageService.sendDeviceMessage(IotDeviceMessage.buildStateOffline(),
                    connectionInfo.getProductKey(), connectionInfo.getDeviceName(), serverId);
            // 发送期间设备在新连接上线（它的上线消息可能先于离线消息）：补发上线消息，避免在线设备被标记为离线
            if (connectionManager.getConnectionInfoByDeviceId(connectionInfo.getDeviceId()) != null) {
                deviceMessageService.sendDeviceMessage(IotDeviceMessage.buildStateUpdateOnline(),
                        connectionInfo.getProductKey(), connectionInfo.getDeviceName(), serverId);
            }
            return null;
        }).onFailure(e -> log.error("[cleanupConnection][清理连接失败]", e));
    }

    // ===================== 发送响应消息 =====================

    /**
     * 发送响应消息
     *
     * @param socket    WebSocket 连接
     * @param requestId 请求 ID
     * @param method    请求方法
     * @param data      响应数据
     */
    private void sendSuccessResponse(ServerWebSocket socket, String requestId, String method, Object data) {
        IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, data, SUCCESS.getCode(), null);
        writeResponse(socket, responseMessage);
    }

    private void sendErrorResponse(ServerWebSocket socket, String requestId, String method, Integer code, String msg) {
        IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, null, code, msg);
        writeResponse(socket, responseMessage);
    }

    /**
     * 写入响应消息
     */
    private void writeResponse(ServerWebSocket socket, IotDeviceMessage responseMessage) {
        byte[] payload = serializer.serialize(responseMessage);
        socket.writeTextMessage(StrUtil.utf8Str(payload));
    }

}
