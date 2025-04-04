package cn.iocoder.yudao.module.iot.net.component.core.message;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * IoT Alink 消息模型
 * <p>
 * 基于阿里云 Alink 协议规范实现的标准消息格式
 *
 * @author haohao
 */
@Data
@Builder
public class IotAlinkMessage {

    /**
     * 消息 ID
     */
    private String id;

    /**
     * 协议版本
     */
    @Builder.Default
    private String version = "1.0";

    /**
     * 消息方法
     */
    private String method;

    /**
     * 消息参数
     */
    private Map<String, Object> params;

    /**
     * 转换为 JSONObject
     *
     * @return JSONObject 对象
     */
    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        json.set("id", id);
        json.set("version", version);
        json.set("method", method);
        json.set("params", params != null ? params : new JSONObject());
        return json;
    }

    /**
     * 转换为 JSON 字符串
     *
     * @return JSON 字符串
     */
    public String toJsonString() {
        return toJsonObject().toString();
    }

    /**
     * 创建设备服务调用消息
     *
     * @param requestId         请求 ID，为空时自动生成
     * @param serviceIdentifier 服务标识符
     * @param params            服务参数
     * @return Alink 消息对象
     */
    public static IotAlinkMessage createServiceInvokeMessage(String requestId, String serviceIdentifier,
                                                             Map<String, Object> params) {
        return IotAlinkMessage.builder()
                .id(requestId != null ? requestId : generateRequestId())
                .method("thing.service." + serviceIdentifier)
                .params(params)
                .build();
    }

    /**
     * 创建设备属性设置消息
     *
     * @param requestId  请求 ID，为空时自动生成
     * @param properties 设备属性
     * @return Alink 消息对象
     */
    public static IotAlinkMessage createPropertySetMessage(String requestId, Map<String, Object> properties) {
        return IotAlinkMessage.builder()
                .id(requestId != null ? requestId : generateRequestId())
                .method("thing.service.property.set")
                .params(properties)
                .build();
    }

    /**
     * 创建设备属性获取消息
     *
     * @param requestId   请求 ID，为空时自动生成
     * @param identifiers 要获取的属性标识符列表
     * @return Alink 消息对象
     */
    public static IotAlinkMessage createPropertyGetMessage(String requestId, String[] identifiers) {
        JSONObject params = new JSONObject();
        params.set("identifiers", identifiers);

        return IotAlinkMessage.builder()
                .id(requestId != null ? requestId : generateRequestId())
                .method("thing.service.property.get")
                .params(params)
                .build();
    }

    /**
     * 创建设备配置设置消息
     *
     * @param requestId 请求 ID，为空时自动生成
     * @param configs   设备配置
     * @return Alink 消息对象
     */
    public static IotAlinkMessage createConfigSetMessage(String requestId, Map<String, Object> configs) {
        return IotAlinkMessage.builder()
                .id(requestId != null ? requestId : generateRequestId())
                .method("thing.service.config.set")
                .params(configs)
                .build();
    }

    /**
     * 创建设备 OTA 升级消息
     *
     * @param requestId 请求 ID，为空时自动生成
     * @param otaInfo   OTA 升级信息
     * @return Alink 消息对象
     */
    public static IotAlinkMessage createOtaUpgradeMessage(String requestId, Map<String, Object> otaInfo) {
        return IotAlinkMessage.builder()
                .id(requestId != null ? requestId : generateRequestId())
                .method("thing.service.ota.upgrade")
                .params(otaInfo)
                .build();
    }

    /**
     * 生成请求 ID
     *
     * @return 请求 ID
     */
    public static String generateRequestId() {
        return IdUtil.fastSimpleUUID();
    }
}