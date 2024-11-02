package cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备数据 Request VO")
@Data
public class IotDeviceDataReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "177")
    private Long deviceId;

    @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED)
    private String identifier;

    @Schema(description = "属性名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

}