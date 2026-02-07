package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.handler.upstream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
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
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlavePendingRequestManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlavePendingRequestManager.PendingRequest;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlavePollScheduler;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

// DONE @AI：逻辑有点多，看看是不是分区域！ => 已按区域划分：认证 / 轮询响应
/**
 * IoT Modbus TCP Slave 上行数据处理器
 * <p>
 * 处理：
 * 1. 自定义 FC 认证
 * 2. 轮询响应（mode=1）→ 点位翻译 → thing.property.post
 * // DONE @AI：不用主动上报；主动上报走标准 tcp 即可
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpSlaveUpstreamHandler {

    private static final String METHOD_AUTH = "auth";

    private final IotDeviceCommonApi deviceApi;
    private final IotDeviceMessageService messageService;
    private final IotModbusDataConverter dataConverter;
    private final IotModbusFrameEncoder frameEncoder;
    private final IotModbusTcpSlaveConnectionManager connectionManager;
    private final IotModbusTcpSlaveConfigCacheService configCacheService;
    private final IotModbusTcpSlavePendingRequestManager pendingRequestManager;
    private final IotModbusTcpSlavePollScheduler pollScheduler;
    private final IotDeviceService deviceService;
    private final String serverId;

    public IotModbusTcpSlaveUpstreamHandler(IotDeviceCommonApi deviceApi,
                                            IotDeviceMessageService messageService,
                                            IotModbusDataConverter dataConverter,
                                            IotModbusFrameEncoder frameEncoder,
                                            IotModbusTcpSlaveConnectionManager connectionManager,
                                            IotModbusTcpSlaveConfigCacheService configCacheService,
                                            IotModbusTcpSlavePendingRequestManager pendingRequestManager,
                                            IotModbusTcpSlavePollScheduler pollScheduler,
                                            IotDeviceService deviceService,
                                            String serverId) {
        this.deviceApi = deviceApi;
        this.messageService = messageService;
        this.dataConverter = dataConverter;
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

        // 2. 自定义功能码（认证等扩展）
        if (StrUtil.isNotEmpty(frame.getCustomData())) {
            handleCustomFrame(socket, frame, frameFormat);
            return;
        }

        // 1.2 未认证连接，丢弃
        // TODO @AI：把 1.2、1.3 拿到 handlePollingResponse 里；是否需要登录，自己知道！
        if (!connectionManager.isAuthenticated(socket)) {
            log.warn("[handleFrame][未认证连接, 丢弃数据, remoteAddress={}]", socket.remoteAddress());
            return;
        }

        // 3. DONE @AI：断言必须是云端轮询（不再支持主动上报）
        // TODO @AI：貌似只能轮询到一次？！
        // 1.3  标准 Modbus 响应（只支持云端轮询模式）
        // TODO @AI：可以把
        ConnectionInfo info = connectionManager.getConnectionInfo(socket);
        if (info == null) {
            log.warn("[handleFrame][已认证但连接信息为空, remoteAddress={}]", socket.remoteAddress());
            return;
        }
        handlePollingResponse(info, frame, frameFormat);
    }

    // ========== 自定义 FC 处理（认证等） ==========

    /**
     * 处理自定义功能码帧
     * <p>
     * 异常分层翻译，参考 {@link cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream.IotHttpAbstractHandler}
     */
    private void handleCustomFrame(NetSocket socket, IotModbusFrame frame, IotModbusFrameFormatEnum frameFormat) {
        try {
            JSONObject json = JSONUtil.parseObj(frame.getCustomData());
            String method = json.getStr("method");
            if (METHOD_AUTH.equals(method)) {
                handleAuth(socket, frame, json, frameFormat);
                return;
            }
            log.warn("[handleCustomFrame][未知 method: {}, frame: slaveId={}, FC={}, customData={}]",
                    method, frame.getSlaveId(), frame.getFunctionCode(), frame.getCustomData());
        } catch (ServiceException e) {
            // 已知业务异常，返回对应的错误码和错误信息
            sendCustomResponse(socket, frame, frameFormat, e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            // 参数校验异常，返回 400 错误
            sendCustomResponse(socket, frame, frameFormat, BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            // 其他未知异常，返回 500 错误
            log.error("[handleCustomFrame][解析自定义 FC 数据失败, frame: slaveId={}, FC={}, customData={}]",
                    frame.getSlaveId(), frame.getFunctionCode(), frame.getCustomData(), e);
            sendCustomResponse(socket, frame, frameFormat,
                    INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
        }
    }

    // TODO @芋艿：在 review 下这个类；
    // TODO @AI：不传递 json，直接在 frame
    /**
     * 处理认证请求
     */
    private void handleAuth(NetSocket socket, IotModbusFrame frame, JSONObject json,
                            IotModbusFrameFormatEnum frameFormat) {
        // TODO @AI：是不是可以 JsonUtils.convert(json, IotDeviceAuthReqDTO.class)；
        JSONObject params = json.getJSONObject("params");
        if (params == null) {
            throw invalidParamException("params 不能为空");
        }
        // DONE @AI：参数判空
        String clientId = params.getStr("clientId");
        String username = params.getStr("username");
        String password = params.getStr("password");
        // TODO @AI：逐个判空；
        if (StrUtil.hasBlank(clientId, username, password)) {
            throw invalidParamException("clientId、username、password 不能为空");
        }

        // 1. 调用认证 API
        IotDeviceAuthReqDTO authReq = new IotDeviceAuthReqDTO()
                .setClientId(clientId).setUsername(username).setPassword(password);
        CommonResult<Boolean> authResult = deviceApi.authDevice(authReq);
        authResult.checkError();
        if (BooleanUtil.isFalse(authResult.getData())) {
            log.warn("[handleAuth][认证失败, clientId={}, username={}]", clientId, username);
            sendCustomResponse(socket, frame, frameFormat, 1, "认证失败");
            return;
        }

        // 2.1 认证成功，查找设备配置
        IotModbusDeviceConfigRespDTO config = configCacheService.findConfigByAuth(clientId, username, password);
        if (config == null) {
            log.info("[handleAuth][认证成功但未找到设备配置, clientId={}, username={}]", clientId, username);
        }
        // 2.2 解析设备信息
        IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(username);
        Assert.notNull(deviceInfo, "解析设备信息失败");
        // 2.3 获取设备信息
        // DONE @AI：IotDeviceService 作为构造参数传入，不通过 SpringUtil.getBean
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        Assert.notNull(device, "设备不存在");

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
        sendCustomResponse(socket, frame, frameFormat, 0, "success");
        log.info("[handleAuth][认证成功, clientId={}, deviceId={}]", clientId,
                config != null ? config.getDeviceId() : device.getId());

        // 5. 直接启动轮询
        if (config != null) {
            pollScheduler.updatePolling(config);
        }
    }

    /**
     * 发送自定义功能码响应
     */
    private void sendCustomResponse(NetSocket socket, IotModbusFrame frame,
                                    IotModbusFrameFormatEnum frameFormat,
                                    int code, String message) {
        JSONObject resp = new JSONObject();
        resp.set("method", METHOD_AUTH);
        resp.set("code", code);
        resp.set("message", message);
        byte[] data = frameEncoder.encodeCustomFrame(frame.getSlaveId(), resp.toString(),
                frameFormat, frame.getTransactionId() != null ? frame.getTransactionId() : 0);
        connectionManager.sendToSocket(socket, data);
    }

    // ========== 轮询响应处理 ==========

    /**
     * 处理轮询响应（云端轮询模式）
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
        int[] rawValues = IotModbusUtils.extractValues(frame);
        if (rawValues == null) {
            log.warn("[handlePollingResponse][提取寄存器值失败, deviceId={}, identifier={}]",
                    info.getDeviceId(), request.getIdentifier());
            return;
        }
        // 1.3 查找点位配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(info.getDeviceId());
        if (config == null || CollUtil.isEmpty(config.getPoints())) {
            return;
        }
        var point = CollUtil.findOne(config.getPoints(), p -> p.getId().equals(request.getPointId()));
        if (point == null) {
            return;
        }

        // 2.1 点位翻译
        Object convertedValue = dataConverter.convertToPropertyValue(rawValues, point);
        // 2.2 上报属性
        Map<String, Object> params = MapUtil.of(request.getIdentifier(), convertedValue);
        IotDeviceMessage message = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), params);
        messageService.sendDeviceMessage(message, info.getProductKey(), info.getDeviceName(), serverId);
        log.debug("[handlePollingResponse][设备={}, 属性={}, 原始值={}, 转换值={}]",
                info.getDeviceId(), request.getIdentifier(), rawValues, convertedValue);
    }

}
