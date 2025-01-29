package cn.iocoder.yudao.module.iot.api.device.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * IoT 设备【属性】数据上报 Request DTO
 */
@Data
public class IotDevicePropertyReportReqDTO extends IotDeviceUpstreamAbstractReqDTO {

    /**
     * 属性参数
     */
    @NotEmpty(message = "属性参数不能为空")
    private Map<String, Object> properties;

}