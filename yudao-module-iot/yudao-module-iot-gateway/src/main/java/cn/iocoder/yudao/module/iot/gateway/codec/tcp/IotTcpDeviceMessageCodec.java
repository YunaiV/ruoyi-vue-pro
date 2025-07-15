package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataDecoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataPackage;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCP {@link IotDeviceMessage} 编解码器
 * <p>
 * 参考 EMQX 设计理念：
 * 1. 高性能编解码
 * 2. 容错机制
 * 3. 缓存优化
 * 4. 监控统计
 * 5. 资源管理
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotTcpDeviceMessageCodec implements IotDeviceMessageCodec {

    /**
     * 编解码器类型
     */
    public static final String TYPE = "tcp";

    // ==================== 方法映射 ====================

    /**
     * 消息方法到功能码的映射
     */
    private static final Map<String, Short> METHOD_TO_CODE_MAP = new ConcurrentHashMap<>();

    /**
     * 功能码到消息方法的映射
     */
    private static final Map<Short, String> CODE_TO_METHOD_MAP = new ConcurrentHashMap<>();

    static {
        // 初始化方法映射
        initializeMethodMappings();
    }

    // ==================== 缓存管理 ====================

    /**
     * JSON 缓存，提升编解码性能
     */
    private final Map<String, JSONObject> jsonCache = new ConcurrentHashMap<>();

    /**
     * 缓存最大大小
     */
    private static final int MAX_CACHE_SIZE = 1000;

    // ==================== 常量定义 ====================

    /**
     * 负载字段名
     */
    public static class PayloadField {
        public static final String TIMESTAMP = "timestamp";
        public static final String MESSAGE_ID = "msgId";
        public static final String DEVICE_ID = "deviceId";
        public static final String PARAMS = "params";
        public static final String DATA = "data";
        public static final String CODE = "code";
        public static final String MESSAGE = "message";
    }

    /**
     * 消息方法映射
     */
    public static class MessageMethod {
        public static final String PROPERTY_POST = "thing.property.post";
        public static final String PROPERTY_SET = "thing.property.set";
        public static final String PROPERTY_GET = "thing.property.get";
        public static final String EVENT_POST = "thing.event.post";
        public static final String SERVICE_INVOKE = "thing.service.invoke";
        public static final String CONFIG_PUSH = "thing.config.push";
        public static final String OTA_UPGRADE = "thing.ota.upgrade";
        public static final String STATE_ONLINE = "thing.state.online";
        public static final String STATE_OFFLINE = "thing.state.offline";
    }

    // ==================== 初始化方法 ====================

    /**
     * 初始化方法映射
     */
    private static void initializeMethodMappings() {
        METHOD_TO_CODE_MAP.put(MessageMethod.PROPERTY_POST, TcpDataPackage.CODE_DATA_UP);
        METHOD_TO_CODE_MAP.put(MessageMethod.PROPERTY_SET, TcpDataPackage.CODE_PROPERTY_SET);
        METHOD_TO_CODE_MAP.put(MessageMethod.PROPERTY_GET, TcpDataPackage.CODE_PROPERTY_GET);
        METHOD_TO_CODE_MAP.put(MessageMethod.EVENT_POST, TcpDataPackage.CODE_EVENT_UP);
        METHOD_TO_CODE_MAP.put(MessageMethod.SERVICE_INVOKE, TcpDataPackage.CODE_SERVICE_INVOKE);
        METHOD_TO_CODE_MAP.put(MessageMethod.CONFIG_PUSH, TcpDataPackage.CODE_DATA_DOWN);
        METHOD_TO_CODE_MAP.put(MessageMethod.OTA_UPGRADE, TcpDataPackage.CODE_DATA_DOWN);
        METHOD_TO_CODE_MAP.put(MessageMethod.STATE_ONLINE, TcpDataPackage.CODE_HEARTBEAT);
        METHOD_TO_CODE_MAP.put(MessageMethod.STATE_OFFLINE, TcpDataPackage.CODE_HEARTBEAT);

        // 反向映射
        METHOD_TO_CODE_MAP.forEach((method, code) -> CODE_TO_METHOD_MAP.put(code, method));
    }

    // ==================== 编解码方法 ====================

    @Override
    public byte[] encode(IotDeviceMessage message) {
        validateEncodeParams(message);

        try {
            if (log.isDebugEnabled()) {
                log.debug("[encode][开始编码 TCP 消息] 方法: {}, 消息ID: {}",
                        message.getMethod(), message.getRequestId());
            }

            // 1. 获取功能码
            short code = getCodeByMethodSafely(message.getMethod());

            // 2. 构建负载
            String payload = buildPayloadOptimized(message);

            // 3. 构建 TCP 数据包
            TcpDataPackage dataPackage = TcpDataPackage.builder()
                    .addr("") // 地址在发送时由调用方设置
                    .code(code)
                    .mid((short) 0) // 消息序号在发送时由调用方设置
                    .payload(payload)
                    .build();

            // 4. 编码为字节流
            Buffer buffer = TcpDataEncoder.encode(dataPackage);
            byte[] result = buffer.getBytes();

            // 5. 统计信息
            if (log.isDebugEnabled()) {
                log.debug("[encode][TCP 消息编码成功] 方法: {}, 数据长度: {}",
                        message.getMethod(), result.length);
            }

            return result;

        } catch (Exception e) {
            log.error("[encode][TCP 消息编码失败] 消息: {}", message, e);
            throw new TcpCodecException("TCP 消息编码失败", e);
        }
    }

    @Override
    public IotDeviceMessage decode(byte[] bytes) {
        validateDecodeParams(bytes);

        try {
            if (log.isDebugEnabled()) {
                log.debug("[decode][开始解码 TCP 消息] 数据长度: {}", bytes.length);
            }

            // 1. 解码 TCP 数据包
            Buffer buffer = Buffer.buffer(bytes);
            TcpDataPackage dataPackage = TcpDataDecoder.decode(buffer);

            // 2. 获取消息方法
            String method = getMethodByCodeSafely(dataPackage.getCode());

            // 3. 解析负载数据
            Object params = parsePayloadOptimized(dataPackage.getPayload());

            // 4. 构建 IoT 设备消息
            IotDeviceMessage message = IotDeviceMessage.requestOf(method, params);

            // 5. 统计信息
            if (log.isDebugEnabled()) {
                log.debug("[decode][TCP 消息解码成功] 方法: {}, 功能码: {}",
                        method, dataPackage.getCode());
            }

            return message;

        } catch (Exception e) {
            log.error("[decode][TCP 消息解码失败] 数据长度: {}, 数据内容: {}",
                    bytes.length, truncateData(bytes, 100), e);
            throw new TcpCodecException("TCP 消息解码失败", e);
        }
    }

    @Override
    public String type() {
        return TYPE;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 验证编码参数
     */
    private void validateEncodeParams(IotDeviceMessage message) {
        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("IoT 设备消息不能为空");
        }
        if (StrUtil.isEmpty(message.getMethod())) {
            throw new IllegalArgumentException("消息方法不能为空");
        }
    }

    /**
     * 验证解码参数
     */
    private void validateDecodeParams(byte[] bytes) {
        if (Objects.isNull(bytes) || bytes.length == 0) {
            throw new IllegalArgumentException("待解码数据不能为空");
        }
        if (bytes.length > 1024 * 1024) { // 1MB 限制
            throw new IllegalArgumentException("数据包过大，超过1MB限制");
        }
    }

    /**
     * 安全获取功能码
     */
    private short getCodeByMethodSafely(String method) {
        Short code = METHOD_TO_CODE_MAP.get(method);
        if (code == null) {
            log.warn("[getCodeByMethodSafely][未知的消息方法: {}，使用默认功能码]", method);
            return TcpDataPackage.CODE_DATA_UP; // 默认为数据上报
        }
        return code;
    }

    /**
     * 安全获取消息方法
     */
    private String getMethodByCodeSafely(short code) {
        String method = CODE_TO_METHOD_MAP.get(code);
        if (method == null) {
            log.warn("[getMethodByCodeSafely][未知的功能码: {}，使用默认方法]", code);
            return MessageMethod.PROPERTY_POST; // 默认为属性上报
        }
        return method;
    }

    /**
     * 优化的负载构建
     */
    private String buildPayloadOptimized(IotDeviceMessage message) {
        // 使用缓存键
        String cacheKey = message.getMethod() + "_" + message.getRequestId();
        JSONObject cachedPayload = jsonCache.get(cacheKey);

        if (cachedPayload != null) {
            // 更新时间戳
            cachedPayload.set(PayloadField.TIMESTAMP, System.currentTimeMillis());
            return cachedPayload.toString();
        }

        // 创建新的负载
        JSONObject payload = new JSONObject();

        // 添加基础字段
        addToPayloadIfNotNull(payload, PayloadField.MESSAGE_ID, message.getRequestId());
        addToPayloadIfNotNull(payload, PayloadField.DEVICE_ID, message.getDeviceId());
        addToPayloadIfNotNull(payload, PayloadField.PARAMS, message.getParams());
        addToPayloadIfNotNull(payload, PayloadField.DATA, message.getData());
        addToPayloadIfNotNull(payload, PayloadField.CODE, message.getCode());
        addToPayloadIfNotEmpty(payload, PayloadField.MESSAGE, message.getMsg());

        // 添加时间戳
        payload.set(PayloadField.TIMESTAMP, System.currentTimeMillis());

        // 缓存管理
        if (jsonCache.size() < MAX_CACHE_SIZE) {
            jsonCache.put(cacheKey, payload);
        } else {
            cleanJsonCacheIfNeeded();
        }

        return payload.toString();
    }

    /**
     * 优化的负载解析
     */
    private Object parsePayloadOptimized(String payload) {
        if (StrUtil.isEmpty(payload)) {
            return null;
        }

        try {
            // 尝试从缓存获取
            JSONObject cachedJson = jsonCache.get(payload);
            if (cachedJson != null) {
                return cachedJson.containsKey(PayloadField.PARAMS) ? cachedJson.get(PayloadField.PARAMS) : cachedJson;
            }

            // 解析 JSON 对象
            JSONObject jsonObject = JSONUtil.parseObj(payload);

            // 缓存解析结果
            if (jsonCache.size() < MAX_CACHE_SIZE) {
                jsonCache.put(payload, jsonObject);
            }

            return jsonObject.containsKey(PayloadField.PARAMS) ? jsonObject.get(PayloadField.PARAMS) : jsonObject;

        } catch (JSONException e) {
            log.warn("[parsePayloadOptimized][负载解析为JSON失败，返回原始字符串] 负载: {}", payload);
            return payload;
        } catch (Exception e) {
            log.error("[parsePayloadOptimized][负载解析异常] 负载: {}", payload, e);
            return payload;
        }
    }

    /**
     * 添加非空值到负载
     */
    private void addToPayloadIfNotNull(JSONObject json, String key, Object value) {
        if (ObjectUtil.isNotNull(value)) {
            json.set(key, value);
        }
    }

    /**
     * 添加非空字符串到负载
     */
    private void addToPayloadIfNotEmpty(JSONObject json, String key, String value) {
        if (StrUtil.isNotEmpty(value)) {
            json.set(key, value);
        }
    }

    /**
     * 清理JSON缓存
     */
    private void cleanJsonCacheIfNeeded() {
        if (jsonCache.size() > MAX_CACHE_SIZE) {
            // 清理一半的缓存
            int clearCount = jsonCache.size() / 2;
            jsonCache.entrySet().removeIf(entry -> clearCount > 0 && Math.random() < 0.5);

            if (log.isDebugEnabled()) {
                log.debug("[cleanJsonCacheIfNeeded][JSON 缓存已清理] 当前缓存大小: {}", jsonCache.size());
            }
        }
    }

    /**
     * 截断数据用于日志输出
     */
    private String truncateData(byte[] data, int maxLength) {
        if (data.length <= maxLength) {
            return new String(data, StandardCharsets.UTF_8);
        }

        byte[] truncated = new byte[maxLength];
        System.arraycopy(data, 0, truncated, 0, maxLength);
        return new String(truncated, StandardCharsets.UTF_8) + "...(截断)";
    }

    // ==================== 自定义异常 ====================

    /**
     * TCP 编解码异常
     */
    public static class TcpCodecException extends RuntimeException {
        public TcpCodecException(String message) {
            super(message);
        }

        public TcpCodecException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}