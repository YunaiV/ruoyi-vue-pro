package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 项目报价保存 Request VO")
@Data
@ToString(callSuper = true)
public class ProjectScheduleSaveReqVO {
    @Schema(description = "安排单 id", example = "1", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "项目 id", example = "1", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long projectId;

    @Schema(description = "安排单名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "项目名字")
    @NotNull(message = "安排单的名字不能为空")
    private String name;

    @Schema(description = "状态, 已提交、已作废、已采用", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    @NotNull(message = "状态, 已提交、已作废、已采用不能为空")
    private String status = "1";

    @Schema(description = "实验名目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<ProjectCategoryWithSupplyAndChargeItemVO>  categoryList;

}
