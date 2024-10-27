package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 物模型消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThingModelMessage {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 扩展功能的参数
     */
    private Object sys;

    /**
     * 请求方法 例如：thing.event.property.post
     */
    private String method;

    /**
     * 请求参数
     */
    private Object params;

    /**
     * 属性上报时间戳
     */
    private Long time;

    /**
     * 设备信息
     */
    private String productKey;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备 key
     */
    private String deviceKey;

    /**
     * 转换为 Map 类型
     */
    public Map<String, Object> dataToMap() {
        Map<String, Object> mapData = new HashMap<>();
        if (params instanceof Map) {
            ((Map<?, ?>) params).forEach((key, value) -> mapData.put(key.toString(), value));
        }
        return mapData;
    }
}
