package cn.iocoder.yudao.module.iot.core.topic.event;

import lombok.Data;

/**
 * IoT 设备事件上报 Request DTO
 * <p>
 * 用于 thing.event.post 消息的 params 参数
 *
 * @author 芋道源码
 * @see <a href="http://help.aliyun.com/zh/marketplace/device-reporting-events">阿里云 - 设备上报事件</a>
 */
@Data
public class IotDeviceEventPostReqDTO {

    /**
     * 事件标识符
     */
    private String identifier;

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
     * @param identifier 事件标识符
     * @param value   事件值
     * @return DTO 对象
     */
    public static IotDeviceEventPostReqDTO of(String identifier, Object value) {
        return of(identifier, value, null);
    }

    /**
     * 创建事件上报 DTO（带时间）
     *
     * @param identifier 事件标识符
     * @param value   事件值
     * @param time    上报时间
     * @return DTO 对象
     */
    public static IotDeviceEventPostReqDTO of(String identifier, Object value, Long time) {
        return new IotDeviceEventPostReqDTO().setIdentifier(identifier).setValue(value).setTime(time);
    }

}
