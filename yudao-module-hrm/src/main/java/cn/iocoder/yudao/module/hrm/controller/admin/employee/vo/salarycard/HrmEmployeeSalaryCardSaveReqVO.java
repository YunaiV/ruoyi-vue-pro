package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.salarycard;

import cn.iocoder.yudao.module.hrm.framework.operatelog.core.HrmBankCardNumberParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工工资卡保存 Request VO")
@Data
public class HrmEmployeeSalaryCardSaveReqVO {

    @Schema(description = "工资卡编号", example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工不能为空")
    private Long employeeId;

    @Schema(description = "银行卡号", requiredMode = Schema.RequiredMode.REQUIRED, example = "622202600001")
    @NotBlank(message = "银行卡号不能为空")
    @Size(max = 64, message = "银行卡号不能超过 64 个字符")
    @DiffLogField(name = "银行卡号", function = HrmBankCardNumberParseFunction.NAME)
    private String bankCardNumber;

    @Schema(description = "开户地区编号", example = "440300")
    @DiffLogField(name = "开户地区编号")
    private Integer bankAreaId;

    @Schema(description = "银行名称", example = "招商银行")
    @Size(max = 64, message = "银行名称不能超过 64 个字符")
    @DiffLogField(name = "银行名称")
    private String bankName;

    @Schema(description = "开户支行名称", example = "科技园支行")
    @Size(max = 128, message = "开户支行名称不能超过 128 个字符")
    @DiffLogField(name = "开户支行名称")
    private String bankBranchName;

}
