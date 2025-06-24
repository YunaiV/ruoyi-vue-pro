package cn.iocoder.yudao.module.huawei.smarthome.dto.device;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@ApiModel("华为智能家居 - 设备服务 VO")
@Data
public class DeviceServiceVO {

    @ApiModelProperty(value = "服务类型", required = true, example = "switch")
    private String serviceType;

    @ApiModelProperty(value = "服务 ID", required = true, example = "acSwitch")
    private String serviceId;

    @ApiModelProperty(value = "特性集", required = true)
    private List<DeviceCharacteristicVO> characteristics;
}
