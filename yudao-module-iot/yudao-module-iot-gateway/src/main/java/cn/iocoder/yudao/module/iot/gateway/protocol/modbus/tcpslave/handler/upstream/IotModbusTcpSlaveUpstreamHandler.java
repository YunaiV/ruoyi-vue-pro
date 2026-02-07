package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusModeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.IotModbusDataConverter;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusResponseParser;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlavePendingRequestManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlavePendingRequestManager.PendingRequest;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.net.NetSocket;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.BiConsumer;

// TODO @AI：逻辑有点多，看看是不是分区域！
/**
 * IoT Modbus TCP Slave 上行数据处理器
 * <p>
 * 处理：
 * 1. 自定义 FC 认证
 * 2. 轮询响应（mode=1）→ 点位翻译 → thing.property.post
 * 3. 主动上报（mode=2）→ 透传 property.report TODO @AI：这种模式，应该不用支持；因为主动上报，都走标准的 tcp 即可；
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpSlaveUpstreamHandler {

    private final IotDeviceCommonApi deviceApi;
    private final IotDeviceMessageService messageService;
    private final IotModbusDataConverter dataConverter;
    private final IotModbusFrameCodec frameCodec;
    private final IotModbusTcpSlaveConnectionManager connectionManager;
    private final IotModbusTcpSlaveConfigCacheService configCacheService;
    private final IotModbusTcpSlavePendingRequestManager pendingRequestManager;
    private final String serverId;

    /**
     * 认证成功回调：(deviceId, config) → 启动轮询等
     */
    @Setter
    private BiConsumer<Long, IotModbusDeviceConfigRespDTO> onAuthSuccess;

    public IotModbusTcpSlaveUpstreamHandler(IotDeviceCommonApi deviceApi,
                                            IotDeviceMessageService messageService,
                                            IotModbusDataConverter dataConverter,
                                            IotModbusFrameCodec frameCodec,
                                            IotModbusTcpSlaveConnectionManager connectionManager,
                                            IotModbusTcpSlaveConfigCacheService configCacheService,
                                            IotModbusTcpSlavePendingRequestManager pendingRequestManager,
                                            String serverId) {
        this.deviceApi = deviceApi;
        this.messageService = messageService;
        this.dataConverter = dataConverter;
        this.frameCodec = frameCodec;
        this.connectionManager = connectionManager;
        this.configCacheService = configCacheService;
        this.pendingRequestManager = pendingRequestManager;
        this.serverId = serverId;
    }

    /**
     * 处理帧
     */
    public void handleFrame(NetSocket socket, IotModbusFrame frame, IotModbusFrameFormatEnum frameFormat) {
        if (frame == null) {
            return;
        }
        // 1.1 自定义功能码（认证等扩展）
        if (StrUtil.isNotEmpty(frame.getCustomData())) {
            handleCustomFrame(socket, frame, frameFormat);
            return;
        }
        // 1.2 异常响应
        if (frame.isException()) {
            // TODO @AI：这种需要返回一个结果给 modbus client？
            log.warn("[handleFrame][设备异常响应, slaveId={}, FC={}, exceptionCode={}]",
                    frame.getSlaveId(), frame.getFunctionCode(), frame.getExceptionCode());
            return;
        }
        // 1.3 未认证连接，丢弃
        if (!connectionManager.isAuthenticated(socket)) {
            // TODO @AI：这种需要返回一个结果给 modbus client？
            log.warn("[handleFrame][未认证连接, 丢弃数据, remoteAddress={}]", socket.remoteAddress());
            return;
        }

        // TODO @AI：获取不到，看看要不要也打个告警；然后
        // 2. 标准 Modbus 响应
        ConnectionInfo info = connectionManager.getConnectionInfo(socket);
        if (info == null) {
            return;
        }
        // TODO @AI：可以断言下，必须是云端轮询；
        if (info.getMode() != null && info.getMode().equals(IotModbusModeEnum.ACTIVE_REPORT.getMode())) {
            // mode=2：主动上报，透传
            handleActiveReport(info, frame);
        } else {
            // mode=1：云端轮询，匹配 PendingRequest
            handlePollingResponse(info, frame, frameFormat);
        }
    }

    /**
     * 处理自定义功能码帧
     */
    private void handleCustomFrame(NetSocket socket, IotModbusFrame frame, IotModbusFrameFormatEnum frameFormat) {
        try {
            // TODO @AI：直接使用 JsonUtils 去解析出 IotDeviceMessage
            JSONObject json = JSONUtil.parseObj(frame.getCustomData());
            String method = json.getStr("method");
            // TODO @AI： method 枚举下；
            if ("auth".equals(method)) {
                handleAuth(socket, frame, json, frameFormat);
                return;
            }
            // TODO @AI：把 frame 都打印下；
            log.warn("[handleCustomFrame][未知 method: {}]", method);
        } catch (Exception e) {
            // TODO @AI：各种情况的翻译；看看怎么弄比较合适；是不是要用 fc 自定义的 callback 下？
            log.error("[handleCustomFrame][解析自定义 FC 数据失败]", e);
        }
    }

    /**
     * 处理认证请求
     */
    private void handleAuth(NetSocket socket, IotModbusFrame frame, JSONObject json,
                            IotModbusFrameFormatEnum frameFormat) {
        // TODO @AI：参数为空的校验；
        JSONObject params = json.getJSONObject("params");
        if (params == null) {
            sendAuthResponse(socket, frame, frameFormat, 1, "params 为空");
            return;
        }
        // TODO @AI：参数判空；
        String clientId = params.getStr("clientId");
        String username = params.getStr("username");
        String password = params.getStr("password");

        try {
            // 1. 调用认证 API
            IotDeviceAuthReqDTO authReq = new IotDeviceAuthReqDTO()
                    .setClientId(clientId).setUsername(username).setPassword(password);
            CommonResult<Boolean> authResult = deviceApi.authDevice(authReq);
            // TODO @AI：应该不用 close 吧？！
            // TODO @AI：BooleanUtils.isFalse
            if (authResult == null || !authResult.isSuccess() || !Boolean.TRUE.equals(authResult.getData())) {
                log.warn("[handleAuth][认证失败, clientId={}, username={}]", clientId, username);
                sendAuthResponse(socket, frame, frameFormat, 1, "认证失败");
                socket.close();
                return;
            }

            // 2. 认证成功，查找设备配置（通过 username 作为 deviceName 查找）
            // TODO 根据实际的认证模型优化查找逻辑
            // TODO @AI：通过 device
            IotModbusDeviceConfigRespDTO config = configCacheService.findConfigByAuth(clientId, username, password);
            if (config == null) {
                // 退而求其次，遍历缓存查找
                log.info("[handleAuth][认证成功但未找到设备配置, clientId={}, username={}]", clientId, username);
            }
            // 2.2 解析设备信息
            IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(username);
            Assert.notNull(deviceInfo, "解析设备信息失败");
            // 2.3 获取设备信息
            // TODO @AI：这里要优化下，不要通过 spring 这样注入；
            IotDeviceService deviceService = SpringUtil.getBean(IotDeviceService.class);
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            Assert.notNull(device, "设备不存在");
            // TODO @AI：校验 frameFormat 是否一致；不一致，连接也失败；

            // 3. 注册连接
            ConnectionInfo connectionInfo = new ConnectionInfo()
                    .setDeviceId(device.getId())
                    .setSlaveId(frame.getSlaveId())
                    .setFrameFormat(frameFormat)
                    .setMode(config != null ? config.getMode() : IotModbusModeEnum.POLLING.getMode());

            if (config != null) {
                connectionInfo.setDeviceId(config.getDeviceId())
                        .setProductKey(config.getProductKey())
                        .setDeviceName(config.getDeviceName());
            }
            connectionManager.registerConnection(socket, connectionInfo);

            // 4. 发送认证成功响应
            sendAuthResponse(socket, frame, frameFormat, 0, "success");
            log.info("[handleAuth][认证成功, clientId={}, deviceId={}]", clientId,
                    config != null ? config.getDeviceId() : "unknown");

            // 5. 回调：启动轮询等
            // TODO @AI：是不是不要 callback，而是主动调用！
            if (onAuthSuccess != null && config != null) {
                onAuthSuccess.accept(config.getDeviceId(), config);
            }
        } catch (Exception e) {
            log.error("[handleAuth][认证异常]", e);
            sendAuthResponse(socket, frame, frameFormat, 1, "认证异常");
            socket.close();
        }
    }

    /**
     * 发送认证响应
     */
    private void sendAuthResponse(NetSocket socket, IotModbusFrame frame,
                                  IotModbusFrameFormatEnum frameFormat,
                                  int code, String message) {
        // TODO @AI：不一定用 auth response；而是 custom？
        JSONObject resp = new JSONObject();
        resp.set("method", "auth");
        resp.set("code", code);
        resp.set("message", message);
        byte[] data = frameCodec.encodeCustomFrame(frame.getSlaveId(), resp.toString(),
                frameFormat, frame.getTransactionId() != null ? frame.getTransactionId() : 0);
        connectionManager.sendToSocket(socket, data);
    }

    /**
     * 处理轮询响应（mode=1）
     */
    private void handlePollingResponse(ConnectionInfo info, IotModbusFrame frame,
                                       IotModbusFrameFormatEnum frameFormat) {
        // 1.1 匹配 PendingRequest
        PendingRequest request = pendingRequestManager.matchResponse(
                info.getDeviceId(), frame, frameFormat);
        if (request == null) {
            log.debug("[handlePollingResponse][未匹配到 PendingRequest, deviceId={}, FC={}]",
                    info.getDeviceId(), frame.getFunctionCode());
            return;
        }
        // 1.2 提取寄存器值
        int[] rawValues = IotModbusResponseParser.extractValues(frame);
        if (rawValues == null) {
            log.warn("[handlePollingResponse][提取寄存器值失败, deviceId={}, identifier={}]",
                    info.getDeviceId(), request.getIdentifier());
            return;
        }
        // 1.3 查找点位配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(info.getDeviceId());
        if (config == null || config.getPoints() == null) {
            return;
        }
        // TODO @AI：findone arrayUtil；
        var point = config.getPoints().stream()
                .filter(p -> p.getId().equals(request.getPointId()))
                .findFirst().orElse(null);
        if (point == null) {
            return;
        }

        // TODO @AI：拆成 2.1、2.2
        // 4. 点位翻译 → 上报
        Object convertedValue = dataConverter.convertToPropertyValue(rawValues, point);
        Map<String, Object> params = MapUtil.of(request.getIdentifier(), convertedValue);
        IotDeviceMessage message = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), params);
        messageService.sendDeviceMessage(message, info.getProductKey(), info.getDeviceName(), serverId);
        log.debug("[handlePollingResponse][设备={}, 属性={}, 原始值={}, 转换值={}]",
                info.getDeviceId(), request.getIdentifier(), rawValues, convertedValue);
    }

    // TODO @AI：不需要这个逻辑；
    /**
     * 处理主动上报（mode=2）
     * 设备直接上报 property.report 格式：{propertyId: value}，不做点位翻译
     */
    @SuppressWarnings("unchecked")
    private void handleActiveReport(ConnectionInfo info, IotModbusFrame frame) {
        // mode=2 下设备上报标准 Modbus 帧，但由于没有点位翻译，
        // 这里暂时将原始寄存器值以 FC+地址 为 key 上报
        int[] rawValues = IotModbusResponseParser.extractValues(frame);
        if (rawValues == null) {
            return;
        }

        // 简单上报：以 "register_FC{fc}" 作为属性名
        String propertyKey = "register_FC" + frame.getFunctionCode();
        Map<String, Object> params = MapUtil.of(propertyKey, rawValues);
        IotDeviceMessage message = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), params);
        messageService.sendDeviceMessage(message, info.getProductKey(), info.getDeviceName(), serverId);

        log.debug("[handleActiveReport][设备={}, FC={}, 原始值={}]",
                info.getDeviceId(), frame.getFunctionCode(), rawValues);
    }

}
