package cn.iocoder.yudao.module.iot.api.device.dto.control.upstream;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * IoT 设备【属性】上报 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDevicePropertyReportReqDTO extends IotDeviceUpstreamAbstractReqDTO {

    /**
     * 属性参数
     */
    @NotEmpty(message = "属性参数不能为空")
    private Map<String, Object> properties;

}