package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 销售线索更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesleadUpdateReqVO extends SalesleadBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Long id;

    @Schema(description = "竞争对手报价", example = "[]")
    private List<SalesleadCompetitorItemVO> competitorQuotations = new ArrayList<>();

    @Schema(description = "客户方案", example = "[]")
    private List<SalesleadCustomerPlanItemVO> customerPlans = new ArrayList<>();
}
