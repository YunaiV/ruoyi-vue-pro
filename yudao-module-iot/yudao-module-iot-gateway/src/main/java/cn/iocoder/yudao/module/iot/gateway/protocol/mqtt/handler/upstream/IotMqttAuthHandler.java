package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.mqtt.MqttEndpoint;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.lang.Assert;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_AUTH_FAIL;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * IoT 网关 MQTT 认证处理器
 * <p>
 * 处理 MQTT CONNECT 事件，完成设备认证、连接注册、上线通知
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttAuthHandler extends IotMqttAbstractHandler {

    private final IotDeviceCommonApi deviceApi;
    private final IotDeviceService deviceService;
    private final String serverId;

    public IotMqttAuthHandler(IotMqttConnectionManager connectionManager,
                              IotDeviceMessageService deviceMessageService,
                              IotDeviceCommonApi deviceApi,
                              String serverId) {
        super(connectionManager, deviceMessageService);
        this.deviceApi = deviceApi;
        this.deviceService = SpringUtil.getBean(IotDeviceService.class);
        this.serverId = serverId;
    }

    /**
     * 处理 MQTT 连接（认证）请求
     *
     * @param endpoint MQTT 连接端点
     * @return 认证是否成功
     */
    @SuppressWarnings("DataFlowIssue")
    public boolean handleAuthenticationRequest(MqttEndpoint endpoint) {
        String clientId = endpoint.clientIdentifier();
        String username = endpoint.auth() != null ? endpoint.auth().getUsername() : null;
        String password = endpoint.auth() != null ? endpoint.auth().getPassword() : null;
        log.debug("[handleConnect][设备连接请求，客户端 ID: {}，用户名: {}，地址: {}]",
                clientId, username, connectionManager.getEndpointAddress(endpoint));

        try {
            // 1.1 解析认证参数
            Assert.notBlank(clientId, "clientId 不能为空");
            Assert.notBlank(username, "username 不能为空");
            Assert.notBlank(password, "password 不能为空");
            // 1.2 构建认证参数
            IotDeviceAuthReqDTO authParams = new IotDeviceAuthReqDTO()
                    .setClientId(clientId)
                    .setUsername(username)
                    .setPassword(password);

            // 2.1 执行认证
            CommonResult<Boolean> authResult = deviceApi.authDevice(authParams);
            authResult.checkError();
            if (BooleanUtil.isFalse(authResult.getData())) {
                throw exception(DEVICE_AUTH_FAIL);
            }
            // 2.2 解析设备信息
            IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(username);
            Assert.notNull(deviceInfo, "解析设备信息失败");
            // 2.3 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            Assert.notNull(device, "设备不存在");

            // 3.1 注册连接
            registerConnection(endpoint, device, clientId);
            // 3.2 发送设备上线消息
            sendOnlineMessage(device);
            log.info("[handleConnect][设备认证成功，建立连接，客户端 ID: {}，用户名: {}]", clientId, username);
            return true;
        } catch (Exception e) {
            log.warn("[handleConnect][设备认证失败，拒绝连接，客户端 ID: {}，用户名: {}，错误: {}]",
                    clientId, username, e.getMessage());
            return false;
        }
    }

    /**
     * 注册连接
     */
    private void registerConnection(MqttEndpoint endpoint, IotDeviceRespDTO device, String clientId) {
        IotMqttConnectionManager.ConnectionInfo connectionInfo = new IotMqttConnectionManager.ConnectionInfo()
                .setDeviceId(device.getId())
                .setProductKey(device.getProductKey())
                .setDeviceName(device.getDeviceName())
                .setRemoteAddress(connectionManager.getEndpointAddress(endpoint));
        connectionManager.registerConnection(endpoint, connectionInfo);
    }

    /**
     * 发送设备上线消息
     */
    private void sendOnlineMessage(IotDeviceRespDTO device) {
        IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
        deviceMessageService.sendDeviceMessage(onlineMessage, device.getProductKey(),
                device.getDeviceName(), serverId);
        log.info("[sendOnlineMessage][设备上线，设备 ID: {}，设备名称: {}]", device.getId(), device.getDeviceName());
    }

}
