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
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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

    public IotModbusTcpServerUpstreamHandler(IotDeviceCommonApi deviceApi,
                                            IotDeviceMessageService messageService,
                                            IotModbusFrameEncoder frameEncoder,
                                            IotModbusTcpServerConnectionManager connectionManager,
                                            IotModbusTcpServerConfigCacheService configCacheService,
                                            IotModbusTcpServerPendingRequestManager pendingRequestManager,
                                            IotModbusTcpServerPollScheduler pollScheduler,
                                            IotDeviceService deviceService,
                                            String serverId) {
        this.deviceApi = deviceApi;
        this.messageService = messageService;
        this.frameEncoder = frameEncoder;
        this.connectionManager = connectionManager;
        this.configCacheService = configCacheService;
        this.pendingRequestManager = pendingRequestManager;
        this.pollScheduler = pollScheduler;
        this.deviceService = deviceService;
        this.serverId = serverId;
    }

    // ========== 帧处理入口 ==========

    /**
     * 处理帧
     */
    public void handleFrame(NetSocket socket, IotModbusFrame frame, IotModbusFrameFormatEnum frameFormat) {
        if (frame == null) {
            return;
        }
        // 1. 异常响应
        if (frame.isException()) {
            log.warn("[handleFrame][设备异常响应, slaveId={}, FC={}, exceptionCode={}]",
                    frame.getSlaveId(), frame.getFunctionCode(), frame.getExceptionCode());
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

        // 3.1 注册连接
        ConnectionInfo connectionInfo = new ConnectionInfo()
                .setDeviceId(device.getId())
                .setProductKey(deviceInfo.getProductKey())
                .setDeviceName(deviceInfo.getDeviceName())
                .setSlaveId(frame.getSlaveId())
                .setFrameFormat(frameFormat);
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
     */
    private void handlePollingResponse(NetSocket socket, IotModbusFrame frame,
                                       IotModbusFrameFormatEnum frameFormat) {
        // 1. 获取连接信息（未认证连接丢弃）
        ConnectionInfo info = connectionManager.getConnectionInfo(socket);
        if (info == null) {
            log.warn("[handlePollingResponse][未认证连接, 丢弃数据, remoteAddress={}]", socket.remoteAddress());
            return;
        }

        // 2.1 匹配 PendingRequest
        PendingRequest request = pendingRequestManager.matchResponse(
                info.getDeviceId(), frame, frameFormat);
        if (request == null) {
            log.debug("[handlePollingResponse][未匹配到 PendingRequest, deviceId={}, FC={}]",
                    info.getDeviceId(), frame.getFunctionCode());
            return;
        }
        // 2.2 提取寄存器值
        int[] rawValues = IotModbusCommonUtils.extractValues(frame);
        if (rawValues == null) {
            log.warn("[handlePollingResponse][提取寄存器值失败, deviceId={}, identifier={}]",
                    info.getDeviceId(), request.getIdentifier());
            return;
        }
        // 2.3 查找点位配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(info.getDeviceId());
        IotModbusPointRespDTO point = IotModbusCommonUtils.findPointById(config, request.getPointId());
        if (point == null) {
            return;
        }

        // 3.1 转换原始值为物模型属性值（点位翻译）
        Object convertedValue = IotModbusCommonUtils.convertToPropertyValue(rawValues, point);
        // 3.2 构造属性上报消息
        Map<String, Object> params = MapUtil.of(request.getIdentifier(), convertedValue);
        IotDeviceMessage message = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), params);

        // 4. 发送到消息总线
        messageService.sendDeviceMessage(message, info.getProductKey(), info.getDeviceName(), serverId);
        log.debug("[handlePollingResponse][设备={}, 属性={}, 原始值={}, 转换值={}]",
                info.getDeviceId(), request.getIdentifier(), rawValues, convertedValue);
    }

}
