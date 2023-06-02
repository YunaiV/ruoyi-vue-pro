package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 项目报价保存 Request VO")
@Data
@ToString(callSuper = true)
public class ProjectQuoteSaveReqVO  {

    @Schema(description = "报价 id", example = "1", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long quoteId;

    @Schema(description = "销售线索 id", example = "1", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
    private Long salesleadId;

    @Schema(description = "报价单的名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotNull(message = "报价单的名字不能为空")
    private String name;

    @Schema(description = "方案 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotNull(message = "方案 URL不能为空")
    private String reportUrl;

    @Schema(description = "折扣(100: 无折扣, 98: 98折)", requiredMode = Schema.RequiredMode.REQUIRED, example = "6647")
    @NotNull(message = "折扣(100: 无折扣, 98: 98折)不能为空")
    private Integer discount;

    @Schema(description = "状态, 已提交、已作废、已采用", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    @NotNull(message = "状态, 已提交、已作废、已采用不能为空")
    private String status = "1";

    @Schema(description = "实验名目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<ProjectCategoryWithSupplyAndChargeItemVO>  categoryList;

}
