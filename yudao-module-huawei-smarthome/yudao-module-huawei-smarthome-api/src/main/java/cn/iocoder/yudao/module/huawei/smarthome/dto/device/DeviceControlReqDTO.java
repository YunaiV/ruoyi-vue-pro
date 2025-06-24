package cn.iocoder.yudao.module.huawei.smarthome.dto.device;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("华为智能家居 - 设备控制请求 DTO")
@Data
public class DeviceControlReqDTO {

    @ApiModelProperty(value = "设备 ID", required = true, example = "8f7e3fbd-8804-4a48-b3d1-df29f05d823c")
    @NotEmpty(message = "设备 ID 不能为空")
    private String deviceId; // 这个会作为路径参数

    @ApiModelProperty(value = "功能 ID (服务 ID)", required = true, example = "acSwitch")
    @NotEmpty(message = "功能 ID 不能为空")
    private String serviceId;

    @ApiModelProperty(value = "控制参数 (Key-Value)", required = true, example = "{\"on\": \"0\"}")
    @NotNull(message = "控制参数不能为空")
    private Map<String, Object> data; // 使用Map<String, Object>以适应不同的控制参数
}
