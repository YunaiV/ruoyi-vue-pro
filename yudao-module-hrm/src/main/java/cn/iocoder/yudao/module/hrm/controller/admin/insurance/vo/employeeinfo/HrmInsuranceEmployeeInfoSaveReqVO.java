package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.employeeinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工参保信息保存 Request VO")
@Data
public class HrmInsuranceEmployeeInfoSaveReqVO {

    @Schema(description = "员工参保信息编号", example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工不能为空")
    private Long employeeId;

    @Schema(description = "是否本地首次缴纳社保", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否本地首次缴纳社保不能为空")
    private Boolean firstSocialSecurity;

    @Schema(description = "是否本地首次缴纳公积金", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否本地首次缴纳公积金不能为空")
    private Boolean firstAccumulationFund;

    @Schema(description = "社保账号", example = "SB20260001")
    @Size(max = 64, message = "社保账号不能超过 64 个字符")
    private String socialSecurityNumber;

    @Schema(description = "公积金账号", example = "GJJ20260001")
    @Size(max = 64, message = "公积金账号不能超过 64 个字符")
    private String accumulationFundNumber;

    @Schema(description = "社保起缴月份")
    private LocalDateTime socialSecurityStartMonth;

    @Schema(description = "社保方案编号", example = "1024")
    private Long schemeId;

}
