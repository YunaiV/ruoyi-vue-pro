package cn.iocoder.yudao.module.iot.plugin.emqx.downstream;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.*;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.MQTT_TOPIC_ILLEGAL;

/**
 * EMQX 插件的 {@link IotDeviceDownstreamHandler} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDeviceDownstreamHandlerImpl implements IotDeviceDownstreamHandler {

    private static final String SYS_TOPIC_PREFIX = "/sys/";

    // TODO @haohao：是不是可以类似 IotDeviceConfigSetVertxHandler 的建议，抽到统一的枚举类
    // TODO @haohao：讨论，感觉 mqtt 和 http，可以做个相对统一的格式哈。；回复 都使用 Alink 格式，方便后续扩展。
    // 设备服务调用 标准 JSON
    // 请求Topic：/sys/${productKey}/${deviceName}/thing/service/${tsl.service.identifier}
    // 响应Topic：/sys/${productKey}/${deviceName}/thing/service/${tsl.service.identifier}_reply
    private static final String SERVICE_TOPIC_PREFIX = "/thing/service/";

    // 设置设备属性 标准 JSON
    // 请求Topic：/sys/${productKey}/${deviceName}/thing/service/property/set
    // 响应Topic：/sys/${productKey}/${deviceName}/thing/service/property/set_reply
    private static final String PROPERTY_SET_TOPIC = "/thing/service/property/set";

    private final MqttClient mqttClient;

    /**
     * 构造函数
     *
     * @param mqttClient MQTT客户端
     */
    public IotDeviceDownstreamHandlerImpl(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    @Override
    public CommonResult<Boolean> invokeDeviceService(IotDeviceServiceInvokeReqDTO reqDTO) {
        log.info("[invokeService][开始调用设备服务][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO));

        // 验证参数
        if (reqDTO.getProductKey() == null || reqDTO.getDeviceName() == null || reqDTO.getIdentifier() == null) {
            log.error("[invokeService][参数不完整][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO));
            return CommonResult.error(MQTT_TOPIC_ILLEGAL.getCode(), MQTT_TOPIC_ILLEGAL.getMsg());
        }

        try {
            // 构建请求主题
            String topic = buildServiceTopic(reqDTO.getProductKey(), reqDTO.getDeviceName(), reqDTO.getIdentifier());
            // 构建请求消息
            String requestId = reqDTO.getRequestId() != null ? reqDTO.getRequestId() : generateRequestId();
            JSONObject request = buildServiceRequest(requestId, reqDTO.getIdentifier(), reqDTO.getParams());
            // 发送消息
            publishMessage(topic, request);

            log.info("[invokeService][调用设备服务成功][requestId: {}][topic: {}]", requestId, topic);
            return CommonResult.success(true);
        } catch (Exception e) {
            log.error("[invokeService][调用设备服务异常][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO), e);
            return CommonResult.error(MQTT_TOPIC_ILLEGAL.getCode(), MQTT_TOPIC_ILLEGAL.getMsg());
        }
    }

    @Override
    public CommonResult<Boolean> getDeviceProperty(IotDevicePropertyGetReqDTO getReqDTO) {
        return CommonResult.success(true);
    }

    @Override
    public CommonResult<Boolean> setDeviceProperty(IotDevicePropertySetReqDTO reqDTO) {
        // 验证参数
        log.info("[setProperty][开始设置设备属性][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO));
        if (reqDTO.getProductKey() == null || reqDTO.getDeviceName() == null) {
            log.error("[setProperty][参数不完整][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO));
            return CommonResult.error(MQTT_TOPIC_ILLEGAL.getCode(), MQTT_TOPIC_ILLEGAL.getMsg());
        }

        try {
            // 构建请求主题
            String topic = buildPropertySetTopic(reqDTO.getProductKey(), reqDTO.getDeviceName());
            // 构建请求消息
            String requestId = reqDTO.getRequestId() != null ? reqDTO.getRequestId() : generateRequestId();
            JSONObject request = buildPropertySetRequest(requestId, reqDTO.getProperties());
            // 发送消息
            publishMessage(topic, request);

            log.info("[setProperty][设置设备属性成功][requestId: {}][topic: {}]", requestId, topic);
            return CommonResult.success(true);
        } catch (Exception e) {
            log.error("[setProperty][设置设备属性异常][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO), e);
            return CommonResult.error(MQTT_TOPIC_ILLEGAL.getCode(), MQTT_TOPIC_ILLEGAL.getMsg());
        }
    }

    @Override
    public CommonResult<Boolean> setDeviceConfig(IotDeviceConfigSetReqDTO setReqDTO) {
        return CommonResult.success(true);
    }

    @Override
    public CommonResult<Boolean> upgradeDeviceOta(IotDeviceOtaUpgradeReqDTO upgradeReqDTO) {
        return CommonResult.success(true);
    }

    /**
     * 构建服务调用主题
     */
    private String buildServiceTopic(String productKey, String deviceName, String serviceIdentifier) {
        return SYS_TOPIC_PREFIX + productKey + "/" + deviceName + SERVICE_TOPIC_PREFIX + serviceIdentifier;
    }

    /**
     * 构建属性设置主题
     */
    private String buildPropertySetTopic(String productKey, String deviceName) {
        return SYS_TOPIC_PREFIX + productKey + "/" + deviceName + PROPERTY_SET_TOPIC;
    }

    // TODO @haohao：这个，后面搞个对象，会不会好点哈？
    /**
     * 构建服务调用请求
     */
    private JSONObject buildServiceRequest(String requestId, String serviceIdentifier, Map<String, Object> params) {
        return new JSONObject()
                .set("id", requestId)
                .set("version", "1.0")
                .set("method", "thing.service." + serviceIdentifier)
                .set("params", params != null ? params : new JSONObject());
    }

    /**
     * 构建属性设置请求
     */
    private JSONObject buildPropertySetRequest(String requestId, Map<String, Object> properties) {
        return new JSONObject()
                .set("id", requestId)
                .set("version", "1.0")
                .set("method", "thing.service.property.set")
                .set("params", properties);
    }

    /**
     * 发布 MQTT 消息
     */
    private void publishMessage(String topic, JSONObject payload) {
        mqttClient.publish(
                topic,
                Buffer.buffer(payload.toString()),
                MqttQoS.AT_LEAST_ONCE,
                false,
                false);
        log.info("[publishMessage][发送消息成功][topic: {}][payload: {}]", topic, payload);
    }

    /**
     * 生成请求 ID
     */
    private String generateRequestId() {
        return IdUtil.fastSimpleUUID();
    }

}