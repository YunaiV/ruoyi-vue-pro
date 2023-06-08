package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import cn.iocoder.yudao.module.jl.controller.admin.project.vo.ProjectQuoteBaseVO;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.ProjectQuoteOrScheduleSaveReqVO;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.ProjectQuoteRespVO;
import cn.iocoder.yudao.module.jl.entity.project.Project;
import cn.iocoder.yudao.module.jl.entity.project.ProjectQuote;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 销售线索 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesleadRespVO extends SalesleadBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26580")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    private CustomerRespVO customer;

    private ProjectQuoteRespVO quote;

    @Schema(description = "折扣前总价", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long totalPrice = 10000L;

    @Schema(description = "项目信息", example = "{}")
    private Project project;

    @Schema(description = "竞争对手报价", example = "[]")
    private List<SalesleadCompetitorItemVO> competitorQuotations = new ArrayList<>();

    @Schema(description = "客户方案", example = "[]")
    private List<SalesleadCustomerPlanItemVO> customerPlans = new ArrayList<>();
}
