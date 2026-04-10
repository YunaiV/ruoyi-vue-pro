package cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 设备保养记录新增/修改 Request VO")
@Data
public class MesDvMaintenRecordSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "计划编号", example = "1")
    private Long planId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备不能为空")
    private Long machineryId;

    @Schema(description = "保养时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "保养时间不能为空")
    private LocalDateTime maintenTime;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "备注", example = "测试备注")
    private String remark;

}
