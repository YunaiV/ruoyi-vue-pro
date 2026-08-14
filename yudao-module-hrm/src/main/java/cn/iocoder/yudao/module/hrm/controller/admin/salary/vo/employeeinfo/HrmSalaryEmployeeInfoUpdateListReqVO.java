package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryBatchAdjustTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeReasonEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 批量调薪 Request VO")
@Data
public class HrmSalaryEmployeeInfoUpdateListReqVO {

    @Schema(description = "员工编号列表", example = "[1024, 1025]")
    private List<@NotNull(message = "员工不能为空") Long> employeeIds;

    @Schema(description = "部门编号列表", example = "[100, 101]")
    private List<@NotNull(message = "部门不能为空") Long> deptIds;

    @Schema(description = "调薪方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "调薪方式不能为空")
    @InEnum(value = HrmSalaryBatchAdjustTypeEnum.class, message = "调薪方式必须是 {value}")
    private Integer type;

    @Schema(description = "调整原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "调薪原因不能为空")
    @InEnum(value = HrmSalaryChangeReasonEnum.class, message = "调薪原因必须是 {value}")
    private Integer changeReason;

    @Schema(description = "生效日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "调薪生效日期不能为空")
    private LocalDateTime effectTime;

    @Schema(description = "备注", example = "年度统一调薪")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

    @Schema(description = "薪资项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "调薪项不能为空")
    @Valid
    private List<HrmSalaryOptionValueVO> salaryOptions;

    @AssertTrue(message = "至少需要选择一个部门或员工")
    @JsonIgnore
    public boolean isScopeValid() {
        return CollUtil.isNotEmpty(deptIds) || CollUtil.isNotEmpty(employeeIds);
    }

}
