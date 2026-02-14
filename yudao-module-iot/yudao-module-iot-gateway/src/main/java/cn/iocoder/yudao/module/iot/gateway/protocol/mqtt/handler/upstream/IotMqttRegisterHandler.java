package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.mqtt.MqttEndpoint;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IoT 网关 MQTT 设备注册处理器：处理设备动态注册消息（一型一密）
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttRegisterHandler extends IotMqttAbstractHandler {

    private final IotDeviceCommonApi deviceApi;

    public IotMqttRegisterHandler(IotMqttConnectionManager connectionManager,
                                  IotDeviceMessageService deviceMessageService) {
        super(connectionManager, deviceMessageService);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    /**
     * 处理注册连接
     * <p>
     * 通过 MQTT 连接的 username 解析设备信息，password 作为签名，直接处理设备注册
     *
     * @param endpoint MQTT 连接端点
     * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
     */
    @SuppressWarnings("DataFlowIssue")
    public void handleRegister(MqttEndpoint endpoint) {
        String clientId = endpoint.clientIdentifier();
        String username = endpoint.auth() != null ? endpoint.auth().getUsername() : null;
        String password = endpoint.auth() != null ? endpoint.auth().getPassword() : null;
        String method = IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod();
        String productKey = null;
        String deviceName = null;

        try {
            // 1.1 校验参数
            Assert.notBlank(clientId, "clientId 不能为空");
            Assert.notBlank(username, "username 不能为空");
            Assert.notBlank(password, "password 不能为空");
            IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(username);
            Assert.notNull(deviceInfo, "解析设备信息失败");
            productKey = deviceInfo.getProductKey();
            deviceName = deviceInfo.getDeviceName();
            log.info("[handleRegister][设备注册连接，客户端 ID: {}，设备: {}.{}]",
                    clientId, productKey, deviceName);
            // 1.2 构建注册参数
            IotDeviceRegisterReqDTO params = new IotDeviceRegisterReqDTO()
                    .setProductKey(productKey)
                    .setDeviceName(deviceName)
                    .setSign(password);

            // 2. 调用动态注册 API
            CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(params);
            result.checkError();

            // 3. 接受连接，并发送成功响应
            endpoint.accept(false);
            sendSuccessResponse(endpoint, productKey, deviceName, null, method, result.getData());
            log.info("[handleRegister][注册成功，设备: {}.{}，客户端 ID: {}]", productKey, deviceName, clientId);
        } catch (Exception e) {
            log.warn("[handleRegister][注册失败，客户端 ID: {}，错误: {}]", clientId, e.getMessage());
            // 接受连接，并发送错误响应
            endpoint.accept(false);
            sendErrorResponse(endpoint, productKey, deviceName, null, method,
                    INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        } finally {
            // 注册完成后关闭连接（一型一密只用于获取 deviceSecret，不保持连接）
            endpoint.close();
        }
    }

}
