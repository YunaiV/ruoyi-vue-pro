package cn.iocoder.yudao.module.iot.core.topic.property;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * IoT 设备属性设置 Request DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#PROPERTY_SET} 下行消息的 params 参数
 * <p>
 * 本质是一个 Map，key 为属性标识符，value 为属性值
 *
 * @author 芋道源码
 */
public class IotDevicePropertySetReqDTO extends HashMap<String, Object> {

    public IotDevicePropertySetReqDTO() {
        super();
    }

    public IotDevicePropertySetReqDTO(Map<String, Object> properties) {
        super(properties);
    }

    /**
     * 创建属性设置 DTO
     *
     * @param properties 属性数据
     * @return DTO 对象
     */
    public static IotDevicePropertySetReqDTO of(Map<String, Object> properties) {
        return new IotDevicePropertySetReqDTO(properties);
    }

}
