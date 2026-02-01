package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
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

import java.util.Map;

/**
 * IoT 网关 MQTT 设备注册处理器
 * <p>
 * 处理设备动态注册消息（一型一密）
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttRegisterHandler extends IotMqttAbstractHandler {

    // TODO IotDeviceMessageMethodEnum.DEVICE_REGISTER 计算出来？IotMqttTopicUtils？
    /**
     * register 请求的 topic 后缀
     */
    public static final String REGISTER_TOPIC_SUFFIX = "/thing/auth/register";

    private final IotDeviceCommonApi deviceApi;

    public IotMqttRegisterHandler(IotMqttConnectionManager connectionManager,
                                  IotDeviceMessageService deviceMessageService,
                                  IotDeviceCommonApi deviceApi) {
        super(connectionManager, deviceMessageService);
        // TODO @AI：通过 springutil 处理；
        this.deviceApi = deviceApi;
    }

    /**
     * 判断是否为注册消息
     *
     * @param topic 主题
     * @return 是否为注册消息
     */
    public boolean isRegisterMessage(String topic) {
        // TODO @AI：是不是搞到 IotMqttTopicUtils 里？
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
        // 1.1 基础检查
        if (ArrayUtil.isEmpty(payload)) {
            return;
        }
        // 1.2 解析主题，获取 productKey 和 deviceName
        // TODO @AI：直接断言报错；
        String[] topicParts = topic.split("/");
        if (topicParts.length < 4 || StrUtil.hasBlank(topicParts[2], topicParts[3])) {
            log.warn("[handleRegister][topic({}) 格式不正确]", topic);
            return;
        }
        String productKey = topicParts[2];
        String deviceName = topicParts[3];
        // TODO @AI：直接断言报错；

        // 2. 使用默认编解码器解码消息（设备可能未注册，无法获取 codecType）
        // TODO @AI：使用默认的 json；
        IotDeviceMessage message;
        try {
            message = deviceMessageService.decodeDeviceMessage(payload, DEFAULT_CODEC_TYPE);
            if (message == null) {
                log.warn("[handleRegister][消息解码失败，客户端 ID: {}，主题: {}]", clientId, topic);
                return;
            }
        } catch (Exception e) {
            log.error("[handleRegister][消息解码异常，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage(), e);
            return;
        }

        // 3. 处理设备动态注册请求
        log.info("[handleRegister][收到设备注册消息，设备: {}.{}, 方法: {}]",
                productKey, deviceName, message.getMethod());
        try {
            processRegisterRequest(message, productKey, deviceName, endpoint);
        } catch (Exception e) {
            // TODO @AI：各种情况下的翻译；
            log.error("[handleRegister][消息处理异常，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage(), e);
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
    private void processRegisterRequest(IotDeviceMessage message, String productKey, String deviceName,
                                         MqttEndpoint endpoint) {
        String clientId = endpoint.clientIdentifier();
        String method = IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod();
        try {
            // 1. 解析注册参数
            IotDeviceRegisterReqDTO params = parseRegisterParams(message.getParams());
            if (params == null) {
                log.warn("[processRegisterRequest][注册参数解析失败，客户端 ID: {}]", clientId);
                sendErrorResponse(endpoint, productKey, deviceName, message.getRequestId(), method, "注册参数不完整");
                return;
            }

            // 2. 调用动态注册 API
            CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(params);
            if (result.isError()) {
                log.warn("[processRegisterRequest][注册失败，客户端 ID: {}，错误: {}]", clientId, result.getMsg());
                sendErrorResponse(endpoint, productKey, deviceName, message.getRequestId(), method, result.getMsg());
                return;
            }

            // 3. 发送成功响应（包含 deviceSecret）
            sendSuccessResponse(endpoint, productKey, deviceName, message.getRequestId(), method, result.getData());
            log.info("[processRegisterRequest][注册成功，设备名: {}，客户端 ID: {}]",
                    params.getDeviceName(), clientId);
        } catch (Exception e) {
            log.error("[processRegisterRequest][注册处理异常，客户端 ID: {}]", clientId, e);
            sendErrorResponse(endpoint, productKey, deviceName, message.getRequestId(), method, "注册处理异常");
        }
    }

    // TODO @AI：解析可以简化，参考别的 tcp 对应的
    /**
     * 解析注册参数
     *
     * @param params 参数对象（通常为 Map 类型）
     * @return 注册参数 DTO，解析失败时返回 null
     */
    @SuppressWarnings("unchecked")
    private IotDeviceRegisterReqDTO parseRegisterParams(Object params) {
        if (params == null) {
            return null;
        }
        try {
            // 参数默认为 Map 类型，直接转换
            if (params instanceof Map) {
                Map<String, Object> paramMap = (Map<String, Object>) params;
                return new IotDeviceRegisterReqDTO()
                        .setProductKey(MapUtil.getStr(paramMap, "productKey"))
                        .setDeviceName(MapUtil.getStr(paramMap, "deviceName"))
                        .setProductSecret(MapUtil.getStr(paramMap, "productSecret"));
            }
            // 如果已经是目标类型，直接返回
            if (params instanceof IotDeviceRegisterReqDTO) {
                return (IotDeviceRegisterReqDTO) params;
            }

            // 其他情况尝试 JSON 转换
            return JsonUtils.convertObject(params, IotDeviceRegisterReqDTO.class);
        } catch (Exception e) {
            log.error("[parseRegisterParams][解析注册参数({})失败]", params, e);
            return null;
        }
    }

}
