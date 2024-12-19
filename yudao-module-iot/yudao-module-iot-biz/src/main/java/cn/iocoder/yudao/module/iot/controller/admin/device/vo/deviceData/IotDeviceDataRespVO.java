package cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备数据 Response VO")
@Data
public class IotDeviceDataRespVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "177")
    private Long deviceId;

    @Schema(description = "物模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21816")
    private Long thingModelId;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productKey;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    private String deviceName;

    @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED)
    private String identifier;

    @Schema(description = "属性名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dataType;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "最新值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String value;

}