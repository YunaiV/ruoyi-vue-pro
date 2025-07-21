package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * TCP JSON格式 {@link IotDeviceMessage} 编解码器
 * 
 * 采用纯JSON格式传输，参考EMQX和HTTP模块的数据格式
 * 
 * JSON消息格式：
 * {
 *   "id": "消息ID",
 *   "method": "消息方法",
 *   "deviceId": "设备ID", 
 *   "productKey": "产品Key",
 *   "deviceName": "设备名称",
 *   "params": {...},
 *   "timestamp": 时间戳
 * }
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotTcpJsonDeviceMessageCodec implements IotDeviceMessageCodec {

    public static final String TYPE = "TCP_JSON";

    // ==================== 常量定义 ====================

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public byte[] encode(IotDeviceMessage message) {
        if (message == null || StrUtil.isEmpty(message.getMethod())) {
            throw new IllegalArgumentException("消息或方法不能为空");
        }

        try {
            // 构建JSON消息
            JSONObject jsonMessage = buildJsonMessage(message);

            // 转换为字节数组
            String jsonString = jsonMessage.toString();
            byte[] result = jsonString.getBytes(StandardCharsets.UTF_8);

            if (log.isDebugEnabled()) {
                log.debug("[encode][编码成功] 方法: {}, JSON长度: {}字节, 内容: {}",
                        message.getMethod(), result.length, jsonString);
            }

            return result;
        } catch (Exception e) {
            log.error("[encode][编码失败] 方法: {}", message.getMethod(), e);
            throw new RuntimeException("JSON消息编码失败", e);
        }
    }

    // ==================== 编解码方法 ====================

    @Override
    public IotDeviceMessage decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("待解码数据不能为空");
        }

        try {
            // 转换为JSON字符串
            String jsonString = new String(bytes, StandardCharsets.UTF_8);

            if (log.isDebugEnabled()) {
                log.debug("[decode][开始解码] JSON长度: {}字节, 内容: {}", bytes.length, jsonString);
            }

            // 解析JSON消息
            JSONObject jsonMessage = JSONUtil.parseObj(jsonString);

            // 构建IoT设备消息
            IotDeviceMessage message = parseJsonMessage(jsonMessage);

            if (log.isDebugEnabled()) {
                log.debug("[decode][解码成功] 消息ID: {}, 方法: {}, 设备ID: {}",
                        message.getId(), message.getMethod(), message.getDeviceId());
            }

            return message;
        } catch (Exception e) {
            log.error("[decode][解码失败] 数据长度: {}", bytes.length, e);
            throw new RuntimeException("JSON消息解码失败", e);
        }
    }

    /**
     * 编码数据上报消息
     */
    public byte[] encodeDataReport(Object params, Long deviceId, String productKey, String deviceName) {
        IotDeviceMessage message = createMessage(MessageMethod.PROPERTY_POST, params, deviceId, productKey, deviceName);
        return encode(message);
    }

    /**
     * 编码心跳消息
     */
    public byte[] encodeHeartbeat(Long deviceId, String productKey, String deviceName) {
        IotDeviceMessage message = createMessage(MessageMethod.STATE_ONLINE, null, deviceId, productKey, deviceName);
        return encode(message);
    }

    // ==================== 便捷方法 ====================

    /**
     * 编码事件上报消息
     */
    public byte[] encodeEventReport(Object params, Long deviceId, String productKey, String deviceName) {
        IotDeviceMessage message = createMessage(MessageMethod.EVENT_POST, params, deviceId, productKey, deviceName);
        return encode(message);
    }

    /**
     * 构建JSON消息
     */
    private JSONObject buildJsonMessage(IotDeviceMessage message) {
        JSONObject jsonMessage = new JSONObject();

        // 基础字段
        jsonMessage.set(JsonField.ID, StrUtil.isNotEmpty(message.getId()) ? message.getId() : IdUtil.fastSimpleUUID());
        jsonMessage.set(JsonField.METHOD, message.getMethod());
        jsonMessage.set(JsonField.TIMESTAMP, System.currentTimeMillis());

        // 设备信息
        if (message.getDeviceId() != null) {
            jsonMessage.set(JsonField.DEVICE_ID, message.getDeviceId());
        }

        // 参数
        if (message.getParams() != null) {
            jsonMessage.set(JsonField.PARAMS, message.getParams());
        }

        // 响应码和消息（用于下行消息）
        if (message.getCode() != null) {
            jsonMessage.set(JsonField.CODE, message.getCode());
        }
        if (StrUtil.isNotEmpty(message.getMsg())) {
            jsonMessage.set(JsonField.MESSAGE, message.getMsg());
        }

        return jsonMessage;
    }

    /**
     * 解析JSON消息
     */
    private IotDeviceMessage parseJsonMessage(JSONObject jsonMessage) {
        // 提取基础字段
        String id = jsonMessage.getStr(JsonField.ID);
        String method = jsonMessage.getStr(JsonField.METHOD);
        Object params = jsonMessage.get(JsonField.PARAMS);

        // 创建消息对象
        IotDeviceMessage message = IotDeviceMessage.requestOf(id, method, params);

        // 设置设备信息
        Long deviceId = jsonMessage.getLong(JsonField.DEVICE_ID);
        if (deviceId != null) {
            message.setDeviceId(deviceId);
        }

        // 设置响应信息
        Integer code = jsonMessage.getInt(JsonField.CODE);
        if (code != null) {
            message.setCode(code);
        }

        String msg = jsonMessage.getStr(JsonField.MESSAGE);
        if (StrUtil.isNotEmpty(msg)) {
            message.setMsg(msg);
        }

        // 设置服务ID（基于JSON格式）
        message.setServerId(generateServerId(jsonMessage));

        return message;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 创建消息对象
     */
    private IotDeviceMessage createMessage(String method, Object params, Long deviceId, String productKey, String deviceName) {
        IotDeviceMessage message = IotDeviceMessage.requestOf(method, params);
        message.setDeviceId(deviceId);
        return message;
    }

    /**
     * 生成服务ID
     */
    private String generateServerId(JSONObject jsonMessage) {
        String id = jsonMessage.getStr(JsonField.ID);
        Long deviceId = jsonMessage.getLong(JsonField.DEVICE_ID);
        return String.format("tcp_json_%s_%s", deviceId != null ? deviceId : "unknown",
                StrUtil.isNotEmpty(id) ? id.substring(0, Math.min(8, id.length())) : "noId");
    }

    /**
     * 消息方法常量
     */
    public static class MessageMethod {
        public static final String PROPERTY_POST = "thing.property.post";  // 数据上报
        public static final String STATE_ONLINE = "thing.state.online";    // 心跳
        public static final String EVENT_POST = "thing.event.post";        // 事件上报
        public static final String PROPERTY_SET = "thing.property.set";    // 属性设置
        public static final String PROPERTY_GET = "thing.property.get";    // 属性获取
        public static final String SERVICE_INVOKE = "thing.service.invoke"; // 服务调用
    }

    /**
     * JSON字段名（参考EMQX和HTTP模块格式）
     */
    private static class JsonField {
        public static final String ID = "id";
        public static final String METHOD = "method";
        public static final String DEVICE_ID = "deviceId";
        public static final String PRODUCT_KEY = "productKey";
        public static final String DEVICE_NAME = "deviceName";
        public static final String PARAMS = "params";
        public static final String TIMESTAMP = "timestamp";
        public static final String CODE = "code";
        public static final String MESSAGE = "message";
    }
}
