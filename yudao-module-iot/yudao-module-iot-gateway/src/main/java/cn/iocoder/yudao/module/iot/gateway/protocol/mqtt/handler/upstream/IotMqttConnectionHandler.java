package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.mqtt.MqttEndpoint;
import lombok.extern.slf4j.Slf4j;

// TODO @AI：改成 auth 这样的命名，更好理解；
/**
 * IoT 网关 MQTT 连接处理器
 * <p>
 * 处理 MQTT CONNECT 事件，完成设备认证、连接注册、上线通知
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttConnectionHandler extends IotMqttAbstractHandler {

    // TODO @AI：通过 springutil 去获取！
    private final IotDeviceCommonApi deviceApi;
    private final String serverId;

    public IotMqttConnectionHandler(IotMqttConnectionManager connectionManager,
                                    IotDeviceMessageService deviceMessageService,
                                    IotDeviceCommonApi deviceApi,
                                    String serverId) {
        super(connectionManager, deviceMessageService);
        this.deviceApi = deviceApi;
        this.serverId = serverId;
    }

    @Override
    protected boolean requiresAuthenticated() {
        return false; // 连接阶段不需要已认证
    }

    /**
     * 处理 MQTT 连接请求
     *
     * @param endpoint MQTT 连接端点
     * @return 认证是否成功
     */
    public boolean handleConnect(MqttEndpoint endpoint) {
        // TODO @AI：整个 try catch 下；
        // TODO @AI：是不是参考 IotTcpUpstreamHandler 的代码结构
        String clientId = endpoint.clientIdentifier();
        String username = endpoint.auth() != null ? endpoint.auth().getUsername() : null;
        String password = endpoint.auth() != null ? endpoint.auth().getPassword() : null;
        log.debug("[handleConnect][设备连接请求，客户端 ID: {}，用户名: {}，地址: {}]",
                clientId, username, connectionManager.getEndpointAddress(endpoint));

        // 进行认证
        if (!authenticateDevice(clientId, username, password, endpoint)) {
            log.warn("[handleConnect][设备认证失败，拒绝连接，客户端 ID: {}，用户名: {}]", clientId, username);
            return false;
        }
        log.info("[handleConnect][设备认证成功，建立连接，客户端 ID: {}，用户名: {}]", clientId, username);
        return true;
    }

    /**
     * 在 MQTT 连接时进行设备认证
     *
     * @param clientId 客户端 ID
     * @param username 用户名
     * @param password 密码
     * @param endpoint MQTT 连接端点
     * @return 认证是否成功
     */
    private boolean authenticateDevice(String clientId, String username, String password, MqttEndpoint endpoint) {
        try {
            // 1.1 解析认证参数
            // TODO @AI：断言，统一交给上层打印日志；
            if (StrUtil.hasEmpty(clientId, username, password)) {
                log.warn("[authenticateDevice][认证参数不完整，客户端 ID: {}，用户名: {}]", clientId, username);
                return false;
            }
            // 1.2 构建认证参数
            IotDeviceAuthReqDTO authParams = new IotDeviceAuthReqDTO()
                    .setClientId(clientId)
                    .setUsername(username)
                    .setPassword(password);

            // 2.1 执行认证
            CommonResult<Boolean> authResult = deviceApi.authDevice(authParams);
            // TODO @AI：断言，统一交给上层打印日志；
            if (!authResult.isSuccess() || !BooleanUtil.isTrue(authResult.getData())) {
                log.warn("[authenticateDevice][设备认证失败，客户端 ID: {}，用户名: {}，错误: {}]",
                        clientId, username, authResult.getMsg());
                return false;
            }
            // 2.2 获取设备信息
            IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(username);
            if (deviceInfo == null) {
                log.warn("[authenticateDevice][用户名格式不正确，客户端 ID: {}，用户名: {}]", clientId, username);
                return false;
            }
            // 2.3 获取设备信息
            // TODO @AI：报错需要处理下；
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            if (device == null) {
                log.warn("[authenticateDevice][设备不存在，客户端 ID: {}，用户名: {}]", clientId, username);
                return false;
            }

            // 3.1 注册连接
            registerConnection(endpoint, device, clientId);
            // 3.2 发送设备上线消息
            sendOnlineMessage(device);
            return true;
        } catch (Exception e) {
            log.error("[authenticateDevice][设备认证异常，客户端 ID: {}，用户名: {}]", clientId, username, e);
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
                .setClientId(clientId)
                .setAuthenticated(true)
                .setRemoteAddress(connectionManager.getEndpointAddress(endpoint));
        connectionManager.registerConnection(endpoint, device.getId(), connectionInfo);
    }

    /**
     * 发送设备上线消息
     */
    private void sendOnlineMessage(IotDeviceRespDTO device) {
        try {
            IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
            deviceMessageService.sendDeviceMessage(onlineMessage, device.getProductKey(),
                    device.getDeviceName(), serverId);
            log.info("[sendOnlineMessage][设备上线，设备 ID: {}，设备名称: {}]", device.getId(), device.getDeviceName());
        } catch (Exception e) {
            log.error("[sendOnlineMessage][发送设备上线消息失败，设备 ID: {}，错误: {}]", device.getId(), e.getMessage());
        }
    }

    /**
     * 清理连接
     *
     * @param endpoint MQTT 连接端点
     */
    public void cleanupConnection(MqttEndpoint endpoint) {
        try {
            // 1. 发送设备离线消息
            IotMqttConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(endpoint);
            if (connectionInfo != null) {
                IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
                deviceMessageService.sendDeviceMessage(offlineMessage, connectionInfo.getProductKey(),
                        connectionInfo.getDeviceName(), serverId);
            }

            // 2. 注销连接
            connectionManager.unregisterConnection(endpoint);
        } catch (Exception e) {
            log.error("[cleanupConnection][清理连接失败，客户端 ID: {}，错误: {}]",
                    endpoint.clientIdentifier(), e.getMessage());
        }
    }

}
