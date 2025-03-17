package cn.iocoder.yudao.module.iot.api.device.dto.control.downstream;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * IoT 设备【配置】设置 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceConfigSetReqDTO extends IotDeviceDownstreamAbstractReqDTO {

    /**
     * 配置
     */
    @NotNull(message = "配置不能为空")
    private Map<String, Object> config;

}
