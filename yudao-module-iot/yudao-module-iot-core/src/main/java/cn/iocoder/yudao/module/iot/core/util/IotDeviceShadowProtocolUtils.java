package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.map.MapUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * IoT 设备影子协议工具
 * <p>
 * 内层 method、state 键名、payload 字段等与设备侧透传 JSON 对齐；并提供 metadata 读写。
 *
 * @author 芋道源码
 */
public final class IotDeviceShadowProtocolUtils {

    private IotDeviceShadowProtocolUtils() {
    }

    // ========== 内层 method ==========

    public static final String INNER_METHOD_GET = "get";
    public static final String INNER_METHOD_UPDATE = "update";
    public static final String INNER_METHOD_GET_REPLY = "get_reply";
    public static final String INNER_METHOD_UPDATE_REPLY = "update_reply";

    // ========== state / metadata 键 ==========

    public static final String STATE_DESIRED = "desired";
    public static final String STATE_REPORTED = "reported";
    public static final String METADATA_TIMESTAMP = "timestamp";

    // ========== reply data 键 ==========

    public static final String PAYLOAD_STATE = "state";
    public static final String PAYLOAD_VERSION = "version";

    /**
     * 由内层 method 推导 reply method（get → get_reply，其余 → update_reply）
     */
    public static String toReplyMethod(String innerMethod) {
        if (INNER_METHOD_GET.equals(innerMethod)) {
            return INNER_METHOD_GET_REPLY;
        }
        return INNER_METHOD_UPDATE_REPLY;
    }

    /**
     * 从透传 params 提取内层 method；无法解析时默认 update
     */
    public static String extractInnerMethod(Object params) {
        if (!(params instanceof Map)) {
            return INNER_METHOD_UPDATE;
        }
        Object method = ((Map<?, ?>) params).get("method");
        return method != null ? String.valueOf(method) : INNER_METHOD_UPDATE;
    }

    /**
     * 获取 desired 下某属性的更新时间戳
     *
     * @param metadata   元数据
     * @param identifier 属性标识符
     * @return 时间戳（epoch 秒），不存在返回 null
     */
    public static Long getDesiredTimestamp(Map<String, Object> metadata, String identifier) {
        return getTimestamp(metadata, STATE_DESIRED, identifier);
    }

    /**
     * 获取 reported 下某属性的更新时间戳
     *
     * @param metadata   元数据
     * @param identifier 属性标识符
     * @return 时间戳（epoch 秒），不存在返回 null
     */
    public static Long getReportedTimestamp(Map<String, Object> metadata, String identifier) {
        return getTimestamp(metadata, STATE_REPORTED, identifier);
    }

    /**
     * 写入 desired 下多个属性的更新时间戳（原地修改 metadata）
     *
     * @param metadata  元数据
     * @param keys      属性标识符集合
     * @param timestamp 时间戳（epoch 秒）
     */
    public static void putDesiredTimestamps(Map<String, Object> metadata, Set<String> keys, long timestamp) {
        putTimestamps(metadata, STATE_DESIRED, keys, timestamp);
    }

    /**
     * 写入 reported 下多个属性的更新时间戳（原地修改 metadata）
     *
     * @param metadata  元数据
     * @param keys      属性标识符集合
     * @param timestamp 时间戳（epoch 秒）
     */
    public static void putReportedTimestamps(Map<String, Object> metadata, Set<String> keys, long timestamp) {
        putTimestamps(metadata, STATE_REPORTED, keys, timestamp);
    }

    @SuppressWarnings("unchecked")
    private static Long getTimestamp(Map<String, Object> metadata, String type, String identifier) {
        if (metadata == null) {
            return null;
        }
        Object typeMeta = metadata.get(type);
        if (!(typeMeta instanceof Map)) {
            return null;
        }
        Object keyMeta = ((Map<String, Object>) typeMeta).get(identifier);
        if (!(keyMeta instanceof Map)) {
            return null;
        }
        Object timestamp = ((Map<String, Object>) keyMeta).get(METADATA_TIMESTAMP);
        return timestamp instanceof Number ? ((Number) timestamp).longValue() : null;
    }

    @SuppressWarnings("unchecked")
    private static void putTimestamps(Map<String, Object> metadata, String type, Set<String> keys, long timestamp) {
        Map<String, Object> typeMeta = (Map<String, Object>) metadata.computeIfAbsent(type, k -> new HashMap<>());
        for (String key : keys) {
            typeMeta.put(key, MapUtil.of(METADATA_TIMESTAMP, timestamp));
        }
    }

}
