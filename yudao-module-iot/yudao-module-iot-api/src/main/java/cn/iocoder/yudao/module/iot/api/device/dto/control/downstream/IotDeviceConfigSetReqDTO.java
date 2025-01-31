package cn.iocoder.yudao.module.iot.api.device.dto.control.downstream;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
