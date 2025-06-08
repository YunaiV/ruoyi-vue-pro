package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 认证路由器
 * <p>
 * 处理设备的 MQTT 连接认证和连接状态管理
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttAuthRouter {

    private final IotMqttUpstreamProtocol protocol;
    private final IotDeviceMessageProducer deviceMessageProducer;
    private final IotDeviceTokenService deviceTokenService;
    private final IotDeviceCommonApi deviceCommonApi;

    public IotMqttAuthRouter(IotMqttUpstreamProtocol protocol) {
        this.protocol = protocol;
        this.deviceMessageProducer = SpringUtil.getBean(IotDeviceMessageProducer.class);
        this.deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);
        this.deviceCommonApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    /**
     * 处理设备认证
     *
     * @param clientId 客户端 ID
     * @param username 用户名
     * @param password 密码
     * @return 认证结果
     */
    public boolean authenticate(String clientId, String username, String password) {
        try {
            log.info("[authenticate][开始认证设备][clientId: {}][username: {}]", clientId, username);

            // 1. 参数校验
            if (StrUtil.isEmpty(clientId) || StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
                log.warn("[authenticate][认证参数不完整][clientId: {}][username: {}]", clientId, username);
                return false;
            }

            // 2. 执行认证
            CommonResult<Boolean> result = deviceCommonApi.authDevice(new IotDeviceAuthReqDTO()
                    .setClientId(clientId).setUsername(username).setPassword(password));
            result.checkError();
            if (!Boolean.TRUE.equals(result.getData())) {
                log.warn("[authenticate][设备认证失败][clientId: {}][username: {}]", clientId, username);
                return false;
            }

            log.info("[authenticate][设备认证成功][clientId: {}][username: {}]", clientId, username);
            return true;
        } catch (Exception e) {
            log.error("[authenticate][设备认证异常][clientId: {}][username: {}]", clientId, username, e);
            return false;
        }
    }

    /**
     * 处理设备连接事件
     *
     * @param clientId 客户端 ID
     * @param username 用户名
     */
    public void handleClientConnected(String clientId, String username) {
        try {
            log.info("[handleClientConnected][设备连接][clientId: {}][username: {}]", clientId, username);

            // 解析设备信息并发送上线消息
            handleDeviceStateChange(username, true);
        } catch (Exception e) {
            log.error("[handleClientConnected][处理设备连接事件异常][clientId: {}][username: {}]", clientId, username, e);
        }
    }

    /**
     * 处理设备断开连接事件
     *
     * @param clientId 客户端 ID
     * @param username 用户名
     */
    public void handleClientDisconnected(String clientId, String username) {
        try {
            log.info("[handleClientDisconnected][设备断开连接][clientId: {}][username: {}]", clientId, username);

            // 解析设备信息并发送下线消息
            handleDeviceStateChange(username, false);
        } catch (Exception e) {
            log.error("[handleClientDisconnected][处理设备断开连接事件异常][clientId: {}][username: {}]", clientId, username, e);
        }
    }

    /**
     * 处理设备状态变化
     *
     * @param username 用户名
     * @param online   是否在线
     */
    private void handleDeviceStateChange(String username, boolean online) {
        // 解析设备信息
        if (StrUtil.isEmpty(username) || "undefined".equals(username)) {
            log.warn("[handleDeviceStateChange][用户名为空或未定义][username: {}]", username);
            return;
        }

        IotDeviceAuthUtils.DeviceInfo deviceInfo = deviceTokenService.parseUsername(username);
        if (deviceInfo == null) {
            log.warn("[handleDeviceStateChange][无法解析设备信息][username: {}]", username);
            return;
        }

        try {
            // 发送设备状态消息
            IotDeviceMessage message = IotDeviceMessage.of(
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName(), protocol.getServerId());

            if (online) {
                message = message.ofStateOnline();
                log.info("[handleDeviceStateChange][发送设备上线消息成功][username: {}]", username);
            } else {
                message = message.ofStateOffline();
                log.info("[handleDeviceStateChange][发送设备下线消息成功][username: {}]", username);
            }

            deviceMessageProducer.sendDeviceMessage(message);
        } catch (Exception e) {
            log.error("[handleDeviceStateChange][发送设备状态消息失败][username: {}][online: {}]", username, online, e);
        }
    }

}