package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT OTA 升级记录 Response VO")
@Data
public class IotOtaTaskRecordRespVO {

    // TODO @芋艿：梳理字段

    @Schema(description = "升级记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long firmwareId;

    @Schema(description = "固件版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "v1.0.0")
    private String firmwareVersion;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long taskId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String deviceId;

    @Schema(description = "来源的固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long fromFirmwareId;

    @Schema(description = "来源的固件版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "v1.0.0")
    private String fromFirmwareVersion;

    @Schema(description = "升级状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "升级进度，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer progress;

    @Schema(description = "升级进度描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private String description;

}
