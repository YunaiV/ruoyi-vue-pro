package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.salarycard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工工资卡 Response VO")
@Data
public class HrmEmployeeSalaryCardRespVO {

    @Schema(description = "工资卡编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long employeeId;

    @Schema(description = "银行卡号", example = "622202600001")
    private String bankCardNumber;

    @Schema(description = "开户地区编号", example = "440300")
    private Integer bankAreaId;

    @Schema(description = "开户地区名称", example = "广东省 深圳市")
    private String bankAreaName;

    @Schema(description = "银行名称", example = "招商银行")
    private String bankName;

    @Schema(description = "开户支行名称", example = "科技园支行")
    private String bankBranchName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
