package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 薪资组 Response VO")
@Data
public class HrmSalaryGroupRespVO {

    @Schema(description = "薪资组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "薪资组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "总部薪资组")
    private String name;

    @Schema(description = "月计薪标准", example = "21.75")
    private BigDecimal salaryStandard;

    @Schema(description = "转正、调薪月规则")
    private String changeRule;

    @Schema(description = "计税规则编号", example = "1024")
    private Long taxRuleId;

    @Schema(description = "计税规则名称", example = "工资薪金所得税")
    private String taxRuleName;

    @Schema(description = "适用部门编号列表")
    private List<Long> deptIds;

    @Schema(description = "适用部门名称列表")
    private List<String> deptNames;

    @Schema(description = "适用员工编号列表")
    private List<Long> employeeIds;

    @Schema(description = "适用员工名称列表")
    private List<String> employeeNames;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
