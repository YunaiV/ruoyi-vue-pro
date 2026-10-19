package cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 项目迭代 Response VO")
@Data
public class PmsIterationRespVO {

    @Schema(description = "迭代编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long projectId;

    @Schema(description = "迭代名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "第一期")
    private String name;

    @Schema(description = "负责人用户编号", example = "1")
    private Long ownerUserId;

    @Schema(description = "迭代状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "负责人姓名", example = "芋道源码")
    private String ownerUserName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "迭代目标", example = "完成核心流程")
    private String target;

    @Schema(description = "迭代描述")
    private String description;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "完成进度百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer progress;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
