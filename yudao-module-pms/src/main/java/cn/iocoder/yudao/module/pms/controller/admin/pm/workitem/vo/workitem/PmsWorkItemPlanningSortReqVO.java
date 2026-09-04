package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 待规划和迭代工作项排序 Request VO")
@Data
public class PmsWorkItemPlanningSortReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "迭代编号；为空时表示 Backlog", example = "2048")
    private Long iterationId;

    @Schema(description = "工作项编号列表，顺序即显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "工作项编号列表不能为空")
    private List<@NotNull(message = "工作项编号不能为空") Long> workItemIds;

}
