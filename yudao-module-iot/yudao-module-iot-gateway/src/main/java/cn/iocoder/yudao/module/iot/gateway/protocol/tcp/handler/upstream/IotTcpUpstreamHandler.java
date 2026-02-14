package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.handler.upstream;

import cn.hutool.core.util.BooleanUtil;
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
import cn.hutool.core.lang.Assert;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_AUTH_FAIL;

/**
 * TCP 上行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpUpstreamHandler implements Handler<NetSocket> {

    private static final String AUTH_METHOD = "auth";

    private final String serverId;

    /**
     * TCP 帧编解码器（处理粘包/拆包）
     */
    private final IotTcpFrameCodec codec;
    /**
     * 消息序列化器（处理业务消息序列化/反序列化）
     */
    private final IotMessageSerializer serializer;
    /**
     * TCP 连接管理器
     */
    private final IotTcpConnectionManager connectionManager;

    private final IotDeviceMessageService deviceMessageService;
    private final IotDeviceService deviceService;
    private final IotDeviceCommonApi deviceApi;

    public IotTcpUpstreamHandler(String serverId,
                                 IotTcpFrameCodec codec,
                                 IotMessageSerializer serializer,
                                 IotTcpConnectionManager connectionManager) {
        Assert.notNull(codec, "TCP FrameCodec 必须配置");
        Assert.notNull(serializer, "消息序列化器必须配置");
        Assert.notNull(connectionManager, "连接管理器不能为空");
        this.serverId = serverId;
        this.codec = codec;
        this.serializer = serializer;
        this.connectionManager = connectionManager;
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.deviceService = SpringUtil.getBean(IotDeviceService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void handle(NetSocket socket) {
        String remoteAddress = String.valueOf(socket.remoteAddress());
        log.debug("[handle][设备连接，地址: {}]", remoteAddress);

        // 1. 设置异常和关闭处理器
        socket.exceptionHandler(ex -> {
            log.warn("[handle][连接异常，地址: {}]", remoteAddress, ex);
            socket.close();
        });
        socket.closeHandler(v -> {
            log.debug("[handle][连接关闭，地址: {}]", remoteAddress);
            cleanupConnection(socket);
        });

        // 2.1 设置消息处理器
        Handler<Buffer> messageHandler = buffer -> {
            try {
                processMessage(buffer, socket);
            } catch (Exception e) {
                log.error("[handle][消息处理失败，地址: {}]", remoteAddress, e);
                socket.close();
            }
        };
        // 2.2 使用拆包器处理粘包/拆包
        RecordParser parser = codec.createDecodeParser(messageHandler);
        socket.handler(parser);
        log.debug("[handle][启用 {} 拆包器，地址: {}]", codec.getType(), remoteAddress);
    }

    /**
     * 处理消息
     *
     * @param buffer   消息
     * @param socket   网络连接
     */
    private void processMessage(Buffer buffer, NetSocket socket) {
        IotDeviceMessage message = null;
        try {
            // 1. 反序列化消息
            message = serializer.deserialize(buffer.getBytes());
            if (message == null) {
                sendErrorResponse(socket, null, null, BAD_REQUEST.getCode(), "消息反序列化失败");
                return;
            }

            // 2. 根据消息类型路由处理
            if (AUTH_METHOD.equals(message.getMethod())) {
                // 认证请求
                handleAuthenticationRequest(message, socket);
            } else if (IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod().equals(message.getMethod())) {
                // 设备动态注册请求
                handleRegisterRequest(message, socket);
            } else {
                // 业务消息
                handleBusinessRequest(message, socket);
            }
        } catch (ServiceException e) {
            // 业务异常，返回对应的错误码和错误信息
            log.warn("[processMessage][业务异常，地址: {}，错误: {}]", socket.remoteAddress(), e.getMessage());
            String requestId = message != null ? message.getRequestId() : null;
            String method = message != null ? message.getMethod() : null;
            sendErrorResponse(socket, requestId, method, e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            // 参数校验失败，返回 400
            log.warn("[processMessage][参数校验失败，地址: {}，错误: {}]", socket.remoteAddress(), e.getMessage());
            String requestId = message != null ? message.getRequestId() : null;
            String method = message != null ? message.getMethod() : null;
            sendErrorResponse(socket, requestId, method, BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            // 其他异常，返回 500，并重新抛出让上层关闭连接
            log.error("[processMessage][处理消息失败，地址: {}]", socket.remoteAddress(), e);
            String requestId = message != null ? message.getRequestId() : null;
            String method = message != null ? message.getMethod() : null;
            sendErrorResponse(socket, requestId, method,
                    INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
            throw e;
        }
    }

    /**
     * 处理认证请求
     *
     * @param message  消息信息
     * @param socket   网络连接
     */
    @SuppressWarnings("DuplicatedCode")
    private void handleAuthenticationRequest(IotDeviceMessage message, NetSocket socket) {
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
     * @param socket   网络连接
     * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
     */
    @SuppressWarnings("DuplicatedCode")
    private void handleRegisterRequest(IotDeviceMessage message, NetSocket socket) {
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
        sendSuccessResponse(socket, message.getRequestId(),
                IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), result.getData());
        log.info("[handleRegisterRequest][注册成功，地址: {}，设备名: {}]",
                socket.remoteAddress(), params.getDeviceName());
    }

    /**
     * 处理业务请求
     *
     * @param message  消息信息
     * @param socket   网络连接
     */
    private void handleBusinessRequest(IotDeviceMessage message, NetSocket socket) {
        // 1. 获取认证信息并处理业务消息
        IotTcpConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);
        if (connectionInfo == null) {
            log.error("[handleBusinessRequest][无法获取连接信息，地址: {}]", socket.remoteAddress());
            sendErrorResponse(socket, message.getRequestId(), message.getMethod(),
                    UNAUTHORIZED.getCode(), "设备未认证，无法处理业务消息");
            return;
        }

        // 2. 发送消息到消息总线
        deviceMessageService.sendDeviceMessage(message, connectionInfo.getProductKey(),
                connectionInfo.getDeviceName(), serverId);
        log.info("[handleBusinessRequest][发送消息到消息总线，地址: {}，消息: {}]", socket.remoteAddress(), message);
    }

    /**
     * 注册连接信息
     *
     * @param socket   网络连接
     * @param device   设备
     */
    private void registerConnection(NetSocket socket, IotDeviceRespDTO device) {
        IotTcpConnectionManager.ConnectionInfo connectionInfo = new IotTcpConnectionManager.ConnectionInfo()
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
        IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
        deviceMessageService.sendDeviceMessage(onlineMessage, device.getProductKey(),
                device.getDeviceName(), serverId);
    }

    /**
     * 清理连接
     *
     * @param socket 网络连接
     */
    private void cleanupConnection(NetSocket socket) {
        // 1. 发送离线消息
        IotTcpConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(socket);
        if (connectionInfo != null) {
            IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
            deviceMessageService.sendDeviceMessage(offlineMessage, connectionInfo.getProductKey(),
                    connectionInfo.getDeviceName(), serverId);
        }

        // 2. 注销连接
        connectionManager.unregisterConnection(socket);
    }

    // ===================== 发送响应消息 =====================

    /**
     * 发送成功响应
     *
     * @param socket    网络连接
     * @param requestId 请求 ID
     * @param method    方法名
     * @param data      响应数据
     */
    private void sendSuccessResponse(NetSocket socket, String requestId, String method, Object data) {
        IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, data, SUCCESS.getCode(), null);
        writeResponse(socket, responseMessage);
    }

    /**
     * 发送错误响应
     *
     * @param socket       网络连接
     * @param requestId    请求 ID
     * @param method       方法名
     * @param code         错误码
     * @param msg          错误消息
     */
    private void sendErrorResponse(NetSocket socket, String requestId, String method, Integer code, String msg) {
        IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, null, code, msg);
        writeResponse(socket, responseMessage);
    }

    /**
     * 写入响应到 Socket
     *
     * @param socket          网络连接
     * @param responseMessage 响应消息
     */
    private void writeResponse(NetSocket socket, IotDeviceMessage responseMessage) {
        byte[] serializedData = serializer.serialize(responseMessage);
        Buffer frameData = codec.encode(serializedData);
        socket.write(frameData);
    }

}