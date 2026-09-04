package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 工作项看板排序 Request VO")
@Data
public class PmsWorkItemSortReqVO {

    @Schema(description = "看板状态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "看板状态不能为空")
    private Long statusId;

    @Schema(description = "工作项编号列表，顺序即看板显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "工作项编号列表不能为空")
    private List<@NotNull(message = "工作项编号不能为空") Long> workItemIds;

}
