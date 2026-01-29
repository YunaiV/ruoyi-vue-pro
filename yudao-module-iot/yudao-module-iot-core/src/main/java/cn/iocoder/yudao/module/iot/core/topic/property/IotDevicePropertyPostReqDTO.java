package cn.iocoder.yudao.module.iot.core.topic.property;

import java.util.HashMap;
import java.util.Map;

/**
 * IoT 设备属性上报 Request DTO
 * <p>
 * 用于 thing.property.post 消息的 params 参数
 * <p>
 * 本质是一个 Map，key 为属性标识符，value 为属性值
 *
 * @author 芋道源码
 * @see <a href="http://help.aliyun.com/zh/marketplace/device-reporting-attributes">阿里云 - 设备上报属性</a>
 */
public class IotDevicePropertyPostReqDTO extends HashMap<String, Object> {

    public IotDevicePropertyPostReqDTO() {
        super();
    }

    public IotDevicePropertyPostReqDTO(Map<String, Object> properties) {
        super(properties);
    }

    /**
     * 创建属性上报 DTO
     *
     * @param properties 属性数据
     * @return DTO 对象
     */
    public static IotDevicePropertyPostReqDTO of(Map<String, Object> properties) {
        return new IotDevicePropertyPostReqDTO(properties);
    }

}
