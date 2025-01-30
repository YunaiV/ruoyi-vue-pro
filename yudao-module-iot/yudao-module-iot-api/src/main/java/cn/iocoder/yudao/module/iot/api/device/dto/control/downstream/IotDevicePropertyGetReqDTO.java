package cn.iocoder.yudao.module.iot.api.device.dto.control.downstream;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * IoT 设备【属性】获取 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDevicePropertyGetReqDTO extends IotDeviceDownstreamAbstractReqDTO {

    /**
     * 属性标识数组
     */
    @NotEmpty(message = "属性标识数组不能为空")
    private List<String> identifiers;

}
