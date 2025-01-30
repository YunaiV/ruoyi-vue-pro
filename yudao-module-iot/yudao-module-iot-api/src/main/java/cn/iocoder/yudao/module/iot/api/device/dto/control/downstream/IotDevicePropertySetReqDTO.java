package cn.iocoder.yudao.module.iot.api.device.dto.control.downstream;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * IoT 设备【属性】设置 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDevicePropertySetReqDTO extends IotDeviceDownstreamAbstractReqDTO {

    /**
     * 属性参数
     */
    @NotEmpty(message = "属性参数不能为空")
    private Map<String, Object> properties;

}
