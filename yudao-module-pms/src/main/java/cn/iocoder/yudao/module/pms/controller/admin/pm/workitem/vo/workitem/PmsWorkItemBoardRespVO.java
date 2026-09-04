package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 工作项看板列 Response VO")
@Data
public class PmsWorkItemBoardRespVO {

    @Schema(description = "看板列编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "看板列名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "映射到当前列的状态列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsWorkItemStatusRespVO> statuses;

    @Schema(description = "工作项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsWorkItemRespVO> items;

}
