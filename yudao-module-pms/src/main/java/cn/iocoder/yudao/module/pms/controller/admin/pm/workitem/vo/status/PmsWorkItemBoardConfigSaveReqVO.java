package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 工作项看板配置保存 Request VO")
@Data
public class PmsWorkItemBoardConfigSaveReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "工作项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "工作项类型不能为空")
    @InEnum(PmsWorkItemTypeEnum.class)
    private Integer workItemType;

    @Schema(description = "看板列列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "看板列列表不能为空")
    private List<Board> boards;

    @Schema(description = "管理后台 - PMS 工作项看板列")
    @Data
    public static class Board {

        @Schema(description = "看板列编号", example = "1024")
        private Long id;

        @Schema(description = "看板列名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "进行中")
        @NotBlank(message = "看板列名称不能为空")
        @Size(max = 50, message = "看板列名称不能超过 50 个字符")
        private String name;

        @Schema(description = "关联状态编号列表，可为空", example = "[1024, 2048]")
        @NotNull(message = "关联状态编号列表不能为空")
        private List<Long> statusIds;

    }

}
