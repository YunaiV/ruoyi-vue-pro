package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 销售线索中竞争对手的报价 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SalesleadCompetitorBaseVO {

    @Schema(description = "销售线索 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "15754")
    @NotNull(message = "销售线索 id不能为空")
    private Long salesleadId;

    @Schema(description = "竞争对手", requiredMode = Schema.RequiredMode.REQUIRED, example = "17682")
    @NotNull(message = "竞争对手不能为空")
    private Long competitorId;

    @Schema(description = "竞争对手的报价")
    private Long competitorQuotation;

}
