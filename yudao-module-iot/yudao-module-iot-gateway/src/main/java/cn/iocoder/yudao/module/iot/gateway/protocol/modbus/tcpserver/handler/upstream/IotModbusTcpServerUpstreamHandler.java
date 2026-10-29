package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPendingRequestManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPendingRequestManager.PendingRequest;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerTransactionChannel;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * IoT Modbus TCP Server 上行数据处理器
 * <p>
 * 处理：
 * 1. 自定义 FC 认证
 * 2. 轮询响应 → 点位翻译 → thing.property.post
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpServerUpstreamHandler {

    private static final String METHOD_AUTH = "auth";

    private final IotDeviceCommonApi deviceApi;
    private final IotDeviceMessageService messageService;
    private final IotModbusFrameEncoder frameEncoder;
    private final IotModbusTcpServerConnectionManager connectionManager;
    private final IotModbusTcpServerConfigCacheService configCacheService;
    private final IotModbusTcpServerPendingRequestManager pendingRequestManager;
    private final IotModbusTcpServerPollScheduler pollScheduler;
    private final IotDeviceService deviceService;

    private final String serverId;

    /**
     * TCP 事务 ID 自增器（所有连接的事务通道共享）
     */
    private final AtomicInteger transactionIdCounter;
    /**
     * 请求超时时间（毫秒）
     */
    private final int requestTimeout;

    public IotModbusTcpServerUpstreamHandler(IotDeviceCommonApi deviceApi,
                                            IotDeviceMessageService messageService,
                                            IotModbusFrameEncoder frameEncoder,
                                            IotModbusTcpServerConnectionManager connectionManager,
                                            IotModbusTcpServerConfigCacheService configCacheService,
                                            IotModbusTcpServerPendingRequestManager pendingRequestManager,
                                            IotModbusTcpServerPollScheduler pollScheduler,
                                            IotDeviceService deviceService,
                                            String serverId,
                                            AtomicInteger transactionIdCounter,
                                            int requestTimeout) {
        this.deviceApi = deviceApi;
        this.messageService = messageService;
        this.frameEncoder = frameEncoder;
        this.connectionManager = connectionManager;
        this.configCacheService = configCacheService;
        this.pendingRequestManager = pendingRequestManager;
        this.pollScheduler = pollScheduler;
        this.deviceService = deviceService;
        this.serverId = serverId;
        this.transactionIdCounter = transactionIdCounter;
        this.requestTimeout = requestTimeout;
    }

    // ========== 帧处理入口 ==========

    /**
     * 处理帧（连接的 owner 上调用：RecordParser 回调）
     */
    public void handleFrame(NetSocket socket, IotModbusFrame frame, IotModbusFrameFormatEnum frameFormat) {
        if (frame == null) {
            return;
        }
        // 1. 异常响应：只尝试匹配透传请求，让它以设备的异常响应收尾（例如 83 02）；
        //    标准轮询的异常响应照旧丢弃，它的待响应请求等到期清理
        if (frame.isException()) {
            log.warn("[handleFrame][设备异常响应, slaveId={}, FC={}, exceptionCode={}]",
                    frame.getSlaveId(), frame.getFunctionCode(), frame.getExceptionCode());
            handlePollingResponse(socket, frame, frameFormat);
            return;
        }

        // 2. 情况一：自定义功能码（认证等扩展）
        if (StrUtil.isNotEmpty(frame.getCustomData())) {
            handleCustomFrame(socket, frame, frameFormat);
            return;
        }

        // 3. 情况二：标准 Modbus 响应 → 轮询响应处理
        handlePollingResponse(socket, frame, frameFormat);
    }

    // ========== 自定义 FC 处理（认证等） ==========

    /**
     * 处理自定义功能码帧
     * <p>
     * 异常分层翻译，参考 {@link cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream.IotHttpAbstractHandler}
     */
    private void handleCustomFrame(NetSocket socket, IotModbusFrame frame, IotModbusFrameFormatEnum frameFormat) {
        String method = null;
        try {
            IotDeviceMessage message = JsonUtils.parseObject(frame.getCustomData(), IotDeviceMessage.class);
            if (message == null) {
                throw invalidParamException("自定义 FC 数据解析失败");
            }
            method = message.getMethod();
            if (METHOD_AUTH.equals(method)) {
                handleAuth(socket, frame, frameFormat, message.getParams());
                return;
            }
            log.warn("[handleCustomFrame][未知 method: {}, frame: slaveId={}, FC={}, customData={}]",
                    method, frame.getSlaveId(), frame.getFunctionCode(), frame.getCustomData());
        } catch (ServiceException e) {
            // 已知业务异常，返回对应的错误码和错误信息
            sendCustomResponse(socket, frame, frameFormat, method, e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            // 参数校验异常，返回 400 错误
            sendCustomResponse(socket, frame, frameFormat, method, BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            // 其他未知异常，返回 500 错误
            log.error("[handleCustomFrame][解析自定义 FC 数据失败, frame: slaveId={}, FC={}, customData={}]",
                    frame.getSlaveId(), frame.getFunctionCode(), frame.getCustomData(), e);
            sendCustomResponse(socket, frame, frameFormat, method,
                    INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
        }
    }

    /**
     * 处理认证请求
     */
    @SuppressWarnings("DataFlowIssue")
    private void handleAuth(NetSocket socket, IotModbusFrame frame, IotModbusFrameFormatEnum frameFormat, Object params) {
        // 1. 解析认证参数
        IotDeviceAuthReqDTO request = JsonUtils.convertObject(params, IotDeviceAuthReqDTO.class);
        Assert.notNull(request, "认证参数不能为空");
        Assert.notBlank(request.getUsername(), "username 不能为空");
        Assert.notBlank(request.getPassword(), "password 不能为空");
        // 特殊：考虑到 modbus 消息体积较小，默认 clientId 传递空串
        if (StrUtil.isBlank(request.getClientId())) {
            request.setClientId(IotDeviceAuthUtils.buildClientIdFromUsername(request.getUsername()));
        }
        Assert.notBlank(request.getClientId(), "clientId 不能为空");

        // 2.1 调用认证 API
        CommonResult<Boolean> result = deviceApi.authDevice(request);
        result.checkError();
        if (BooleanUtil.isFalse(result.getData())) {
            log.warn("[handleAuth][认证失败, clientId={}, username={}]", request.getClientId(), request.getUsername());
            sendCustomResponse(socket, frame, frameFormat, METHOD_AUTH, BAD_REQUEST.getCode(), "认证失败");
            return;
        }
        // 2.2 解析设备信息
        IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(request.getUsername());
        Assert.notNull(deviceInfo, "解析设备信息失败");
        // 2.3 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        Assert.notNull(device, "设备不存在");
        // 2.4 加载设备 Modbus 配置，无配置则阻断认证
        IotModbusDeviceConfigRespDTO modbusConfig = configCacheService.loadDeviceConfig(device.getId());
        if (modbusConfig == null) {
            log.warn("[handleAuth][设备 {} 没有 Modbus 点位配置, 拒绝认证]", device.getId());
            sendCustomResponse(socket, frame, frameFormat, METHOD_AUTH, BAD_REQUEST.getCode(), "设备无 Modbus 配置");
            return;
        }
        // 2.5 协议不一致，阻断认证
        if (ObjUtil.notEqual(frameFormat.getFormat(), modbusConfig.getFrameFormat())) {
            log.warn("[handleAuth][设备 {} frameFormat 不一致, 连接协议={}, 设备配置={}，拒绝认证]",
                    device.getId(), frameFormat.getFormat(), modbusConfig.getFrameFormat());
            sendCustomResponse(socket, frame, frameFormat, METHOD_AUTH, BAD_REQUEST.getCode(),
                    "frameFormat 协议不一致");
            return;
        }

        // 3.1 注册连接：事务通道在本连接的 owner（当前 event loop Context）上创建
        Context owner = Vertx.currentContext();
        Assert.notNull(owner, "事务通道必须在连接的 event loop 上创建");
        IotModbusTcpServerTransactionChannel channel = new IotModbusTcpServerTransactionChannel(owner, socket,
                device.getId(), frame.getSlaveId(), frameFormat, frameEncoder, pendingRequestManager,
                transactionIdCounter, requestTimeout);
        ConnectionInfo connectionInfo = new ConnectionInfo()
                .setDeviceId(device.getId())
                .setProductKey(deviceInfo.getProductKey())
                .setDeviceName(deviceInfo.getDeviceName())
                .setSlaveId(frame.getSlaveId())
                .setFrameFormat(frameFormat)
                .setChannel(channel);
        connectionManager.registerConnection(socket, connectionInfo);
        // 3.2 发送上线消息
        IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
        messageService.sendDeviceMessage(onlineMessage, deviceInfo.getProductKey(), deviceInfo.getDeviceName(), serverId);
        // 3.3 发送成功响应
        sendCustomResponse(socket, frame, frameFormat, METHOD_AUTH,
                GlobalErrorCodeConstants.SUCCESS.getCode(), "success");
        log.info("[handleAuth][认证成功, clientId={}, deviceId={}]", request.getClientId(), device.getId());

        // 4. 启动轮询
        pollScheduler.updatePolling(modbusConfig);
    }

    /**
     * 发送自定义功能码响应
     */
    private void sendCustomResponse(NetSocket socket, IotModbusFrame frame,
                                    IotModbusFrameFormatEnum frameFormat,
                                    String method, int code, String message) {
        Map<String, Object> response = MapUtil.<String, Object>builder()
                .put("method", method)
                .put("code", code)
                .put("message", message)
                .build();
        byte[] data = frameEncoder.encodeCustomFrame(frame.getSlaveId(), JsonUtils.toJsonString(response),
                frameFormat, frame.getTransactionId());
        connectionManager.sendToSocket(socket, data);
    }

    // ========== 轮询响应处理 ==========

    /**
     * 处理轮询响应（云端轮询模式）
     * <p>
     * 匹配待响应请求、透传收尾留在连接的 owner 上（与发送、清理同一个线程）；
     * 标准轮询的点位翻译、发送到消息总线会阻塞，交给本连接（事务通道）的 worker 队列按收到的顺序执行
     */
    private void handlePollingResponse(NetSocket socket, IotModbusFrame frame,
                                       IotModbusFrameFormatEnum frameFormat) {
        // 1. 获取连接信息（未认证连接丢弃）
        ConnectionInfo info = connectionManager.getConnectionInfo(socket);
        if (info == null) {
            log.warn("[handlePollingResponse][未认证连接, 丢弃数据, remoteAddress={}]", socket.remoteAddress());
            return;
        }

        // 2.1 匹配 PendingRequest：只匹配这条连接自己发出的请求
        PendingRequest request = pendingRequestManager.matchResponse(
                info.getDeviceId(), frame, frameFormat, info.getChannel());
        if (request == null) {
            log.debug("[handlePollingResponse][未匹配到 PendingRequest, deviceId={}, FC={}]",
                    info.getDeviceId(), frame.getFunctionCode());
            return;
        }
        // 2.2 透传请求：交给事务通道进入唯一终态，不做点位处理
        if (request.getTx() != null) {
            info.getChannel().onRawResponse(frame, request);
            return;
        }

        // 3. 标准轮询：交给本连接的 worker 队列；队列已满时丢弃这次上报（下一轮轮询会再次读取），不断开连接
        info.getChannel().getUpstreamQueue().submit(() -> {
            handlePollingResult(info, frame, request);
            return null;
        }).onFailure(e -> {
            if (e instanceof RejectedExecutionException) {
                log.warn("[handlePollingResponse][轮询上报积压, 丢弃本次上报, deviceId={}, identifier={}, 错误: {}]",
                        info.getDeviceId(), request.getIdentifier(), e.getMessage());
                return;
            }
            log.error("[handlePollingResponse][轮询上报失败, deviceId={}, identifier={}]",
                    info.getDeviceId(), request.getIdentifier(), e);
        });
    }

    /**
     * 处理标准轮询的响应（worker 上执行）：点位翻译 → thing.property.post 发送到消息总线
     *
     * @param info    连接信息
     * @param frame   响应帧
     * @param request 匹配到的待响应请求
     */
    private void handlePollingResult(ConnectionInfo info, IotModbusFrame frame, PendingRequest request) {
        // 1.1 提取寄存器值
        int[] rawValues = IotModbusCommonUtils.extractValues(frame);
        if (rawValues == null) {
            log.warn("[handlePollingResult][提取寄存器值失败, deviceId={}, identifier={}]",
                    info.getDeviceId(), request.getIdentifier());
            return;
        }
        // 1.2 查找点位配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(info.getDeviceId());
        IotModbusPointRespDTO point = IotModbusCommonUtils.findPointById(config, request.getPointId());
        if (point == null) {
            return;
        }

        // 2.1 转换原始值为物模型属性值（点位翻译）
        Object convertedValue = IotModbusCommonUtils.convertToPropertyValue(rawValues, point);
        // 2.2 构造属性上报消息
        Map<String, Object> params = MapUtil.of(request.getIdentifier(), convertedValue);
        IotDeviceMessage message = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), params);

        // 3. 发送到消息总线
        messageService.sendDeviceMessage(message, info.getProductKey(), info.getDeviceName(), serverId);
        log.debug("[handlePollingResult][设备={}, 属性={}, 原始值={}, 转换值={}]",
                info.getDeviceId(), request.getIdentifier(), rawValues, convertedValue);
    }

    // ========== 连接关闭 ==========

    /**
     * 发送设备下线消息（连接的 owner 上调用：closeHandler）
     * <p>
     * 发送会阻塞，作为本连接的最后一个任务交给 worker 队列，排在已收到的轮询上报之后；
     * 执行时设备已在新连接上线就不再发送，发送期间设备在新连接上线则补发上线消息（与 TCP 协议一致）
     *
     * @param info 已移除的连接信息
     * @return 发送结果（在 owner 上完成）；失败只记日志
     */
    public Future<Void> sendOfflineMessage(ConnectionInfo info) {
        return info.getChannel().getUpstreamQueue().<Void>submitLast(() -> {
            if (connectionManager.getConnectionInfoByDeviceId(info.getDeviceId()) != null) {
                return null;
            }
            messageService.sendDeviceMessage(IotDeviceMessage.buildStateOffline(),
                    info.getProductKey(), info.getDeviceName(), serverId);
            if (connectionManager.getConnectionInfoByDeviceId(info.getDeviceId()) != null) {
                messageService.sendDeviceMessage(IotDeviceMessage.buildStateUpdateOnline(),
                        info.getProductKey(), info.getDeviceName(), serverId);
            }
            return null;
        }).onFailure(e -> log.error("[sendOfflineMessage][发送设备下线消息失败, deviceId={}]", info.getDeviceId(), e));
    }

}
