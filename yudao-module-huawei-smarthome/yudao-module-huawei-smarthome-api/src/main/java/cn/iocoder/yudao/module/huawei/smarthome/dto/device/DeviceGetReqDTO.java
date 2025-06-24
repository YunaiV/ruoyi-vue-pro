package cn.iocoder.yudao.module.huawei.smarthome.dto.device;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@ApiModel("华为智能家居 - 查询指定设备信息请求 DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceGetReqDTO {

    @ApiModelProperty(value = "设备 ID 列表 (一次查询最多支持查询 20 个设备信息)", required = true, example = "[\"device-uuid-123\", \"device-uuid-456\"]")
    @NotEmpty(message = "设备 ID 列表不能为空")
    @Size(min = 1, max = 20, message = "一次最多查询 20 个设备")
    private List<String> deviceIds;
}
