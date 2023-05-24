package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 销售线索 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SalesleadBaseVO {

    @Schema(description = "销售线索来源", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "销售线索来源不能为空")
    private String source;

    @Schema(description = "关键需求")
    private String requirement;

    @Schema(description = "预算(元)")
    private Long budget;

    @Schema(description = "报价")
    private Long quotation;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "客户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "11635")
    @NotNull(message = "客户id不能为空")
    private Long customerId;

    @Schema(description = "项目id", example = "8951", nullable = true)
    private Long projectId;

    @Schema(description = "业务类型", example = "课题研究", nullable = true)
    private String businessType;

    @Schema(description = "丢单说明", example = "...", nullable = true)
    private String lostNote;
}
