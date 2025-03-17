package cn.iocoder.yudao.module.iot.api.device.dto.control.downstream;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * IoT 设备【服务】调用 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceServiceInvokeReqDTO extends IotDeviceDownstreamAbstractReqDTO {

    /**
     * 服务标识
     */
    @NotEmpty(message = "服务标识不能为空")
    private String identifier;
    /**
     * 调用参数
     */
    private Map<String, Object> params;

}
