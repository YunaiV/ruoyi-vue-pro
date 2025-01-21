package cn.iocoder.yudao.module.iot.api.device.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * IoT 设备【事件】数据上报 Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IotDeviceEventReportReqDTO {

    // TODO 芋艿：要不要 id
    // TODO 芋艿：要不要 time

    /**
     * 产品标识
     */
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;
    /**
     * 设备名称
     */
    @NotEmpty(message = "设备名称不能为空")
    private String deviceName;

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
