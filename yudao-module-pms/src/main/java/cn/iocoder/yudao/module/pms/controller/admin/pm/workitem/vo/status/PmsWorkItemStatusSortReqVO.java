package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 工作项状态排序 Request VO")
@Data
public class PmsWorkItemStatusSortReqVO {

    @Schema(description = "状态编号列表，顺序即显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "状态编号列表不能为空")
    private List<@NotNull(message = "状态编号不能为空") Long> statusIds;

}
