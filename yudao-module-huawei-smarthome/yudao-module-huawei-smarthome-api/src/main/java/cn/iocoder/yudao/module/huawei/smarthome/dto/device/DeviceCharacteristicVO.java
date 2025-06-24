package cn.iocoder.yudao.module.huawei.smarthome.dto.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("华为智能家居 - 设备特性 VO")
@Data
public class DeviceCharacteristicVO {

    @ApiModelProperty(value = "时间戳 UTC 时间", example = "20151212T121212001Z")
    private String timestamp; // 文档中为可选，但示例中有

    @ApiModelProperty(value = "特性名", required = true, example = "on")
    @JsonProperty("characteristicName") // 确保JSON序列化/反序列化时名称正确
    private String characteristicName;

    @ApiModelProperty(value = "特性值", required = true, example = "1")
    @JsonProperty("characteristicValue")
    private Object characteristicValue; // 特性值类型多样，使用Object
}
