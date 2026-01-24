package cn.iocoder.yudao.module.iot.core.topic.event;

import lombok.Data;

/**
 * IoT 设备事件上报 Request DTO
 * <p>
 * 用于 thing.event.{eventId}.post 消息的 params 参数
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services">阿里云 - 设备上报事件</a>
 */
@Data
public class IotDeviceEventPostReqDTO {

    /**
     * 事件标识符
     */
    private String eventId;

    /**
     * 事件输出参数
     */
    private Object value;

    /**
     * 上报时间（毫秒时间戳，可选）
     */
    private Long time;

    /**
     * 创建事件上报 DTO
     *
     * @param eventId 事件标识符
     * @param value   事件值
     * @return DTO 对象
     */
    public static IotDeviceEventPostReqDTO of(String eventId, Object value) {
        return of(eventId, value, null);
    }

    /**
     * 创建事件上报 DTO（带时间）
     *
     * @param eventId 事件标识符
     * @param value   事件值
     * @param time    上报时间
     * @return DTO 对象
     */
    public static IotDeviceEventPostReqDTO of(String eventId, Object value, Long time) {
        return new IotDeviceEventPostReqDTO()
                .setEventId(eventId)
                .setValue(value)
                .setTime(time);
    }

}
