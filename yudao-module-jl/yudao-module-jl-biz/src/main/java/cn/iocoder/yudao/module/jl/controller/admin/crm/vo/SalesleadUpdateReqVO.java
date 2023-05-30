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

    @Schema(description = "岗位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26580")
    @NotNull(message = "岗位ID不能为空")
    private Long id;

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

    // 线索成交的字段
    @Schema(description = "项目名字")
    private String projectName;

    @Schema(description = "合同文件URL")
    private String contractUrl;

    @Schema(description = "合同文件名")
    private String contractName;

}
