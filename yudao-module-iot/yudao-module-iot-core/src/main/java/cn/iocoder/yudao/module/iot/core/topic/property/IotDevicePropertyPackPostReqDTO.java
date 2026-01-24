package cn.iocoder.yudao.module.iot.core.topic.property;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * IoT 设备属性批量上报 Request DTO
 * <p>
 * 用于 thing.event.property.pack.post 消息的 params 参数
 *
 * @author 芋道源码
 * @see <a href="http://help.aliyun.com/zh/marketplace/gateway-reports-data-in-batches">阿里云 - 网关批量上报数据</a>
 */
@Data
public class IotDevicePropertyPackPostReqDTO {

    // TODO @AI：不用 PropertyValue，直接使用 Object 接收就行！
    /**
     * 网关自身属性
     * <p>
     * key: 属性标识符
     * value: 属性值对象（包含 value 和 time）
     */
    private Map<String, PropertyValue> properties;

    /**
     * 网关自身事件
     * <p>
     * key: 事件标识符
     * value: 事件值对象（包含 value 和 time）
     */
    private Map<String, EventValue> events;

    /**
     * 子设备数据列表
     */
    private List<SubDeviceData> subDevices;

    /**
     * 属性值对象
     */
    @Data
    public static class PropertyValue {

        /**
         * 属性值
         */
        private Object value;

        /**
         * 上报时间（毫秒时间戳）
         */
        private Long time;

    }

    /**
     * 事件值对象
     */
    @Data
    public static class EventValue {

        /**
         * 事件参数
         */
        private Object value;

        /**
         * 上报时间（毫秒时间戳）
         */
        private Long time;

    }

    /**
     * 子设备数据
     */
    @Data
    public static class SubDeviceData {

        /**
         * 子设备标识
         */
        private DeviceIdentity identity;

        /**
         * 子设备属性
         */
        private Map<String, PropertyValue> properties;

        /**
         * 子设备事件
         */
        private Map<String, EventValue> events;

    }

    /**
     * 设备标识
     */
    @Data
    @Accessors(chain = true)
    public static class DeviceIdentity {

        /**
         * 产品标识
         */
        private String productKey;

        /**
         * 设备名称
         */
        private String deviceName;

    }

}
