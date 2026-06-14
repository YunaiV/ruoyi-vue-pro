package cn.iocoder.yudao.module.iot.core.enums.shadow;

import cn.hutool.core.map.MapUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.module.iot.core.enums.shadow.IotDeviceShadowProtocolConstants.*;

/**
 * IoT 设备影子 metadata 读写工具
 * <p>
 * 结构：{ desired: { identifier: { timestamp } }, reported: { ... } }
 *
 * @author 芋道源码
 */
public final class IotDeviceShadowMetadatas {

    private IotDeviceShadowMetadatas() {
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
