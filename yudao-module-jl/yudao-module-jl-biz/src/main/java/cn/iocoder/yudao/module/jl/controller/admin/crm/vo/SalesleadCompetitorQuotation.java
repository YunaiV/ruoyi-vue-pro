package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "销售线索的报价")
@Data
@ToString(callSuper = true)
public class SalesleadCompetitorQuotation {
    @Schema(description = "竞争对手 id")
    private Long competitorId;

    @Schema(description = "报价")
    private Long quotation;
}
