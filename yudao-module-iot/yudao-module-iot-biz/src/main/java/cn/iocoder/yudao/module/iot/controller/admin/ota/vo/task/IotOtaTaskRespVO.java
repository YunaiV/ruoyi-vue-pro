package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task;

import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT OTA 升级任务 Response VO")
@Data
public class IotOtaTaskRespVO implements VO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "升级任务")
    private String name;

    @Schema(description = "任务描述", example = "升级任务")
    private String description;

    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long firmwareId;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "升级范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer deviceScope;

    @Schema(description = "设备总共数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer deviceTotalCount;

    @Schema(description = "设备成功数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "66")
    private Integer deviceSuccessCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-07-08 07:30:00")
    private LocalDateTime createTime;

}
