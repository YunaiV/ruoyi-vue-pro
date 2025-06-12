package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 协议的处理器抽象基类
 * <p>
 * 提供通用的异常处理、参数校验等功能
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class IotMqttAbstractHandler {

    /**
     * 处理 MQTT 消息的模板方法
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    public final void handle(String topic, String payload) {
        try {
            // 1. 前置校验
            if (!validateInput(topic, payload)) {
                return;
            }

            // 2. 执行具体逻辑
            doHandle(topic, payload);

        } catch (Exception e) {
            log.error("[handle][处理 MQTT 消息失败][topic: {}][payload: {}]", topic, payload, e);
            handleException(topic, payload, e);
        }
    }

    /**
     * 具体的处理逻辑，由子类实现
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    protected abstract void doHandle(String topic, String payload);

    /**
     * 输入参数校验
     *
     * @param topic   主题
     * @param payload 消息内容
     * @return 校验是否通过
     */
    protected boolean validateInput(String topic, String payload) {
        if (StrUtil.isBlank(topic)) {
            log.warn("[validateInput][主题为空，忽略消息]");
            return false;
        }

        if (StrUtil.isBlank(payload)) {
            log.warn("[validateInput][消息内容为空][topic: {}]", topic);
            return false;
        }

        return true;
    }

    /**
     * 异常处理
     *
     * @param topic   主题
     * @param payload 消息内容
     * @param e       异常
     */
    protected void handleException(String topic, String payload, Exception e) {
        // 默认实现：记录错误日志
        // 子类可以重写此方法，添加特定的异常处理逻辑
        log.error("[handleException][MQTT 消息处理异常][topic: {}]", topic, e);
    }

    /**
     * 解析主题，获取主题各部分
     *
     * @param topic 主题
     * @return 主题各部分数组，如果解析失败返回 null
     */
    protected String[] parseTopic(String topic) {
        String[] topicParts = topic.split("/");
        if (topicParts.length < 7) {
            log.warn("[parseTopic][主题格式不正确][topic: {}]", topic);
            return null;
        }
        return topicParts;
    }
}