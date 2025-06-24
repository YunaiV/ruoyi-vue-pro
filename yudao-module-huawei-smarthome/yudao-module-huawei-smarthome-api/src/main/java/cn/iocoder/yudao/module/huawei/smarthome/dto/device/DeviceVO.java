package cn.iocoder.yudao.module.huawei.smarthome.dto.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@ApiModel("华为智能家居 - 设备信息 VO")
@Data
public class DeviceVO {

    @ApiModelProperty(value = "设备 ID", required = true, example = "device-uuid-123")
    private String deviceId;

    @ApiModelProperty(value = "设备状态", required = true, example = "online", notes = "枚举: online/offline")
    private String status;

    @ApiModelProperty(value = "空间 ID", required = true, example = "space-uuid-456")
    private String spaceId;

    @ApiModelProperty(value = "房间 ID", required = true, example = "room-uuid-789")
    private String roomId;

    @ApiModelProperty(value = "中枢/主机的设备 ID", required = true, example = "gateway-uuid-000",
            notes = "针对下挂设备；假如就是中枢/主机值等于 deviceId")
    private String gatewayDeviceId;

    @ApiModelProperty(value = "设备基本信息", required = true)
    @JsonProperty("devInfo") // 确保JSON字段名正确
    private DevInfoVO devInfo;

    @ApiModelProperty(value = "服务集", required = true)
    private List<DeviceServiceVO> services;
}
