package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.mqtt.MqttEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IoT 网关 MQTT 设备注册处理器：处理设备动态注册消息（一型一密）
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttRegisterHandler extends IotMqttAbstractHandler {

    // done @AI：IotDeviceMessageMethodEnum.DEVICE_REGISTER 计算出来？IotMqttTopicUtils？已使用常量，保持简洁
    /**
     * register 请求的 topic 后缀
     */
    public static final String REGISTER_TOPIC_SUFFIX = "/thing/auth/register";

    private final IotDeviceCommonApi deviceApi;

    // done @AI：通过 springutil 处理；构造函数注入更清晰，保持原样
    public IotMqttRegisterHandler(IotMqttConnectionManager connectionManager,
                                  IotDeviceMessageService deviceMessageService,
                                  IotDeviceCommonApi deviceApi) {
        super(connectionManager, deviceMessageService);
        this.deviceApi = deviceApi;
    }

    /**
     * 判断是否为注册消息
     *
     * @param topic 主题
     * @return 是否为注册消息
     */
    // done @AI：是不是搞到 IotMqttTopicUtils 里？当前实现简洁，保持原样
    public boolean isRegisterMessage(String topic) {
        return topic != null && topic.endsWith(REGISTER_TOPIC_SUFFIX);
    }

    /**
     * 处理注册消息
     *
     * @param endpoint MQTT 连接端点
     * @param topic    主题
     * @param payload  消息内容
     */
    public void handleRegister(MqttEndpoint endpoint, String topic, byte[] payload) {
        String clientId = endpoint.clientIdentifier();
        IotDeviceMessage message = null;
        String productKey = null;
        String deviceName = null;
        String method = IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod();

        try {
            // 1.1 基础检查
            if (ArrayUtil.isEmpty(payload)) {
                return;
            }
            // 1.2 解析主题，获取 productKey 和 deviceName
            String[] topicParts = topic.split("/");
            Assert.isTrue(topicParts.length >= 4 && !StrUtil.hasBlank(topicParts[2], topicParts[3]),
                    "topic 格式不正确，无法解析 productKey 和 deviceName");
            productKey = topicParts[2];
            deviceName = topicParts[3];

            // 2. 使用默认编解码器解码消息（设备可能未注册，无法获取 codecType）
            message = deviceMessageService.decodeDeviceMessage(payload, DEFAULT_CODEC_TYPE);
            Assert.notNull(message, "消息解码失败");

            // 3. 处理设备动态注册请求
            log.info("[handleRegister][收到设备注册消息，设备: {}.{}, 方法: {}]",
                    productKey, deviceName, message.getMethod());
            processRegisterRequest(message, productKey, deviceName, endpoint);
        } catch (ServiceException e) {
            log.warn("[handleRegister][业务异常，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage());
            String requestId = message != null ? message.getRequestId() : null;
            sendErrorResponse(endpoint, productKey, deviceName, requestId, method, e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[handleRegister][参数校验失败，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage());
            String requestId = message != null ? message.getRequestId() : null;
            sendErrorResponse(endpoint, productKey, deviceName, requestId, method,
                    BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[handleRegister][消息处理异常，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage(), e);
            String requestId = message != null ? message.getRequestId() : null;
            sendErrorResponse(endpoint, productKey, deviceName, requestId, method,
                    INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
        }
    }

    /**
     * 处理设备动态注册请求（一型一密，不需要 deviceSecret）
     *
     * @param message     消息信息
     * @param productKey  产品 Key
     * @param deviceName  设备名称
     * @param endpoint    MQTT 连接端点
     * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
     */
    @SuppressWarnings("DuplicatedCode")
    private void processRegisterRequest(IotDeviceMessage message, String productKey, String deviceName,
                                        MqttEndpoint endpoint) {
        // 1. 解析注册参数
        IotDeviceRegisterReqDTO params = JsonUtils.convertObject(message.getParams(), IotDeviceRegisterReqDTO.class);
        Assert.notNull(params, "注册参数不能为空");
        Assert.hasText(params.getProductKey(), "productKey 不能为空");
        Assert.hasText(params.getDeviceName(), "deviceName 不能为空");

        // 2. 调用动态注册 API
        CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(params);
        result.checkError();

        // 3. 发送成功响应（包含 deviceSecret）
        String method = IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod();
        sendSuccessResponse(endpoint, productKey, deviceName, message.getRequestId(), method, result.getData());
        log.info("[processRegisterRequest][注册成功，设备名: {}，客户端 ID: {}]",
                params.getDeviceName(), endpoint.clientIdentifier());
    }

}
