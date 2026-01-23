package cn.iocoder.yudao.module.iot.core.topic.property;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

// TODO @AI：挂个阿里云的链接，http://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services 的「设备批量上报属性、事件」小节
/**
 * IoT 设备属性批量上报 Request DTO
 * <p>
 * 用于 thing.event.property.pack.post 消息的 params 参数
 * 参考阿里云 Alink 协议
 *
 * @author 芋道源码
 */
@Data
public class IotDevicePropertyPackPostReqDTO {

    // TODO @AI：去掉里面的 time，直接平铺值（可能就是直接的 map）；例如说 "Power"：value， 而不是 PropertyValue "properties": {
    //            "Power": [
    //                {
    //                    "value": "on",
    //                    "time": 1524448722000
    //                },
    //                {
    //                    "value": "off",
    //                    "time": 1524448722001
    //                }
    //            ],
    //            "WF": [
    //                {
    //                    "value": 3,
    //                    "time": 1524448722000
    //                },
    //                {
    //                    "value": 4,
    //                    "time": 1524448722009
    //                }
    //            ]
    //        }

    /**
     * 网关自身属性
     * <p>
     * key: 属性标识符
     * value: 属性值对象（包含 value 和 time）
     */
    private Map<String, PropertyValue> properties;

    // TODO @AI：EventValue {
    //
    //    "method": "thing.event.post",
    //
    //    "version": "1.0",
    //
    //    "params": {
    //
    //        "identifier": "eat",
    //
    //        "params": {
    //
    //            "rice": 100
    //
    //        }
    //
    //    }
    //
    //}


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
