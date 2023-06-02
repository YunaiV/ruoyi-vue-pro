package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 项目报价保存 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectQuoteOrScheduleSaveReqVO extends ProjectQuoteBaseVO {
    @Schema(description = "销售线索 id", example = "1", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long salesleadId;

    @Schema(description = "项目 id", example = "1", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long projectId;

    @Schema(description = "实验名目", example = "[]")
    private List<ProjectCategoryWithSupplyAndChargeItemVO> categoryList;
}
