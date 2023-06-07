package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 销售线索中竞争对手的报价 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SalesleadCompetitorItemVO {

    @Schema(description = "销售线索 id", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Long salesleadId;

    @Schema(description = "竞争对手", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "竞争对手不能为空")
    private Long competitorId;

    @Schema(description = "竞争对手的报价")
    private Long competitorQuotation;

}
