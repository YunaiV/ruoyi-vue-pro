package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 工作项看板配置 Response VO")
@Data
public class PmsWorkItemBoardConfigRespVO {

    @Schema(description = "看板列列表")
    private List<Board> boards;

    @Schema(description = "未放入看板的状态编号列表")
    private List<Long> unassignedStatusIds;

    @Data
    public static class Board {

        @Schema(description = "看板列编号")
        private Long id;

        @Schema(description = "看板列名称")
        private String name;

        @Schema(description = "关联状态编号列表")
        private List<Long> statusIds;
    }

}
