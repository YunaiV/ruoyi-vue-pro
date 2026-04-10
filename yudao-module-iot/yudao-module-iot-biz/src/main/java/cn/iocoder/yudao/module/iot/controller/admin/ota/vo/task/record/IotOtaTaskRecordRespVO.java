package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT OTA 升级任务记录 Response VO")
@Data
public class IotOtaTaskRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long firmwareId;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long taskId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceId;

    @Schema(description = "设备名称", example = "智能开关")
    private String deviceName;

    @Schema(description = "来源的固件编号", example = "1023")
    private Long fromFirmwareId;

    @Schema(description = "来源固件版本", example = "1.0.0")
    private String fromFirmwareVersion;

    @Schema(description = "升级状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "升级进度，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer progress;

    @Schema(description = "升级进度描述", example = "正在下载固件...")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
