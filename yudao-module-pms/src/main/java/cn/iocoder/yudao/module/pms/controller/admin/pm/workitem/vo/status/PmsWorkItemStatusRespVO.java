package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - PMS 工作项看板状态 Response VO")
@Data
public class PmsWorkItemStatusRespVO {

    @Schema(description = "状态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long projectId;

    @Schema(description = "工作项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer workItemType;

    @Schema(description = "状态名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "未开始")
    private String name;

    @Schema(description = "语义状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer statusType;

    @Schema(description = "状态描述", example = "需求已拆分，等待开发")
    private String description;

    @Schema(description = "所属看板列名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "处理中")
    private String boardName;

    @Schema(description = "是否初始状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean defaultStatus;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

}
