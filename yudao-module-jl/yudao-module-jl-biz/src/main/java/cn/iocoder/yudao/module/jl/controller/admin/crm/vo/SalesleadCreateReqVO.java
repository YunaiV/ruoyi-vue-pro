package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 销售线索创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesleadCreateReqVO extends SalesleadBaseVO {
    @Schema(description = "竞争对手的报价，数组")
    private ArrayList<SalesleadCompetitorQuotation> competitorQuotations;;

    @Schema(description = "客户方案 URL")
    private String planUrl;

    @Schema(description = "客户方案文件名")
    private String planDocName;

    @Schema(description = "方案报告 URL")
    private String reportUrl;

    @Schema(description = "客户报告文件名")
    private String reportDocName;

    @Schema(description = "项目售前人员 Id")
    private Long managerId;
}
