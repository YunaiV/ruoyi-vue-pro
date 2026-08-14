package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.employeeinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工参保信息 Response VO")
@Data
public class HrmInsuranceEmployeeInfoRespVO {

    @Schema(description = "员工参保信息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long employeeId;

    @Schema(description = "是否本地首次缴纳社保", example = "true")
    private Boolean firstSocialSecurity;

    @Schema(description = "是否本地首次缴纳公积金", example = "false")
    private Boolean firstAccumulationFund;

    @Schema(description = "社保账号", example = "SB20260001")
    private String socialSecurityNumber;

    @Schema(description = "公积金账号", example = "GJJ20260001")
    private String accumulationFundNumber;

    @Schema(description = "社保起缴月份")
    private LocalDateTime socialSecurityStartMonth;

    @Schema(description = "社保方案编号", example = "1024")
    private Long schemeId;

    @Schema(description = "社保方案名称", example = "深圳标准社保方案")
    private String schemeName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
