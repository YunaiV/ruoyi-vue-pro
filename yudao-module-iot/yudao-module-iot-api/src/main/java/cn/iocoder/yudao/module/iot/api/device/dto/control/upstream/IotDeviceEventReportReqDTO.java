package cn.iocoder.yudao.module.iot.api.device.dto.control.upstream;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * IoT 设备【事件】上报 Request DTO
 */
@Data
public class IotDeviceEventReportReqDTO extends IotDeviceUpstreamAbstractReqDTO {

    /**
     * 事件标识
     */
    @NotEmpty(message = "事件标识不能为空")
    private String identifier;
    /**
     * 事件参数
     */
    @NotEmpty(message = "事件参数不能为空")
    private Map<String, Object> params;

}
