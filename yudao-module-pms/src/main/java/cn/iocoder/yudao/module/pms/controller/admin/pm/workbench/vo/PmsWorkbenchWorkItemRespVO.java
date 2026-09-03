package cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 工作台工作项 Response VO")
@Data
public class PmsWorkbenchWorkItemRespVO {

    @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long projectId;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "商城项目")
    private String projectName;

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer projectType;

    @Schema(description = "工作项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer type;

    @Schema(description = "工作项序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer serialNumber;

    @Schema(description = "工作项名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "优化登录页")
    private String name;

    @Schema(description = "优先级", example = "2")
    private Integer priority;

    @Schema(description = "负责人用户编号", example = "1024")
    private Long assigneeUserId;

    @Schema(description = "负责人姓名", example = "芋道源码")
    private String assigneeUserName;

    @Schema(description = "创建人用户编号", example = "2048")
    private Long creatorUserId;

    @Schema(description = "创建人姓名", example = "源码")
    private String creatorUserName;

    @Schema(description = "状态编号", example = "1")
    private Long statusId;

    @Schema(description = "状态名称", example = "进行中")
    private String statusName;

    @Schema(description = "语义状态", example = "2")
    private Integer status;

    @Schema(description = "进度", example = "60")
    private Integer progress;

    @Schema(description = "开始时间", example = "2026-01-01 10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-01-31 18:00:00")
    private LocalDateTime endTime;

    @Schema(description = "是否可编辑", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean writeStatus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
