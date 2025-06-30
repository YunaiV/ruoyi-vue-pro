package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task;

import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "管理后台 - IoT OTA 升级任务 Response VO")
public class IotOtaUpgradeTaskRespVO implements VO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "升级任务")
    private String name;

    @Schema(description = "任务描述", example = "升级任务")
    private String description;

    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long firmwareId;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "升级范围", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"1", "2"})
    private Integer scope;

    @Schema(description = "设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceCount;

    @Schema(description = "选中的设备编号数组", example = "1024")
    private List<Long> deviceIds;

    @Schema(description = "选中的设备名字数组", example = "1024")
    private List<String> deviceNames;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-07-08 07:30:00")
    private LocalDateTime createTime;

}
