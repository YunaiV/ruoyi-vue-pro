package cn.iocoder.yudao.module.huawei.smarthome.dto.device;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@ApiModel("华为智能家居 - 设备列表响应 DTO")
@Data
public class DeviceListRespDTO {

    @ApiModelProperty(value = "设备集合")
    private List<DeviceVO> devices;
}
