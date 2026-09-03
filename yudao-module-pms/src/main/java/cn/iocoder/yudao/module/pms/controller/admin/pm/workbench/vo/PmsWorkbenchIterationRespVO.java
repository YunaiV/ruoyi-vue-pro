package cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 工作台迭代 Response VO")
@Data
public class PmsWorkbenchIterationRespVO {

    @Schema(description = "迭代编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long projectId;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "商城项目")
    private String projectName;

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer projectType;

    @Schema(description = "迭代名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "第一个迭代")
    private String name;

    @Schema(description = "迭代状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    @Schema(description = "开始时间", example = "2026-01-01 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-01-31 23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "迭代目标", example = "完成登录模块")
    private String target;

}
