package cn.iocoder.yudao.module.iot.net.component.emqx.downstream;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.*;
import cn.iocoder.yudao.module.iot.net.component.core.constants.IotDeviceTopicEnum;
import cn.iocoder.yudao.module.iot.net.component.core.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.net.component.core.message.IotAlinkMessage;
import cn.iocoder.yudao.module.iot.net.component.core.util.IotNetComponentCommonUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.MQTT_TOPIC_ILLEGAL;

/**
 * EMQX 网络组件的 {@link IotDeviceDownstreamHandler} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDeviceDownstreamHandlerImpl implements IotDeviceDownstreamHandler {

    /**
     * MQTT 客户端
     */
    private final MqttClient mqttClient;

    /**
     * 构造函数
     *
     * @param mqttClient MQTT 客户端
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
            String topic = IotDeviceTopicEnum.buildServiceTopic(reqDTO.getProductKey(), reqDTO.getDeviceName(),
                    reqDTO.getIdentifier());

            // 构建请求消息
            String requestId = StrUtil.isNotEmpty(reqDTO.getRequestId()) ? reqDTO.getRequestId()
                    : IotNetComponentCommonUtils.generateRequestId();
            IotAlinkMessage message = IotAlinkMessage.createServiceInvokeMessage(
                    requestId, reqDTO.getIdentifier(), reqDTO.getParams());

            // 发送消息
            publishMessage(topic, message.toJsonObject());

            log.info("[invokeService][调用设备服务成功][requestId: {}][topic: {}]", requestId, topic);
            return CommonResult.success(true);
        } catch (Exception e) {
            log.error("[invokeService][调用设备服务异常][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO), e);
            return CommonResult.error(MQTT_TOPIC_ILLEGAL.getCode(), MQTT_TOPIC_ILLEGAL.getMsg());
        }
    }

    @Override
    public CommonResult<Boolean> getDeviceProperty(IotDevicePropertyGetReqDTO getReqDTO) {
        // 暂未实现，返回成功
        return CommonResult.success(true);
    }

    @Override
    public CommonResult<Boolean> setDeviceProperty(IotDevicePropertySetReqDTO reqDTO) {
        log.info("[setProperty][开始设置设备属性][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO));

        // 验证参数
        if (reqDTO.getProductKey() == null || reqDTO.getDeviceName() == null) {
            log.error("[setProperty][参数不完整][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO));
            return CommonResult.error(MQTT_TOPIC_ILLEGAL.getCode(), MQTT_TOPIC_ILLEGAL.getMsg());
        }

        try {
            // 构建请求主题
            String topic = IotDeviceTopicEnum.buildPropertySetTopic(reqDTO.getProductKey(), reqDTO.getDeviceName());

            // 构建请求消息
            String requestId = StrUtil.isNotEmpty(reqDTO.getRequestId()) ? reqDTO.getRequestId()
                    : IotNetComponentCommonUtils.generateRequestId();
            IotAlinkMessage message = IotAlinkMessage.createPropertySetMessage(requestId, reqDTO.getProperties());

            // 发送消息
            publishMessage(topic, message.toJsonObject());

            log.info("[setProperty][设置设备属性成功][requestId: {}][topic: {}]", requestId, topic);
            return CommonResult.success(true);
        } catch (Exception e) {
            log.error("[setProperty][设置设备属性异常][reqDTO: {}]", JSONUtil.toJsonStr(reqDTO), e);
            return CommonResult.error(MQTT_TOPIC_ILLEGAL.getCode(), MQTT_TOPIC_ILLEGAL.getMsg());
        }
    }

    @Override
    public CommonResult<Boolean> setDeviceConfig(IotDeviceConfigSetReqDTO setReqDTO) {
        // 暂未实现，返回成功
        return CommonResult.success(true);
    }

    @Override
    public CommonResult<Boolean> upgradeDeviceOta(IotDeviceOtaUpgradeReqDTO upgradeReqDTO) {
        // 暂未实现，返回成功
        return CommonResult.success(true);
    }

    /**
     * 发布 MQTT 消息
     *
     * @param topic   主题
     * @param payload 消息内容
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
}