package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 员工绩效档案 Response VO")
@Data
public class HrmPerformanceArchiveEmployeeRespVO {

    @Schema(description = "员工编号", example = "1024")
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "工号", example = "HRM001")
    private String jobNumber;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "职位名称", example = "Java 工程师")
    private String postName;

    @Schema(description = "手机号码", example = "15601691300")
    private String mobile;

    @Schema(description = "员工状态", example = "1")
    private Integer employeeStatus;

    @Schema(description = "聘用形式", example = "1")
    private Integer employeeType;

    @Schema(description = "最近考核编号", example = "2048")
    private Long latestAssessmentId;

    @Schema(description = "最近考核计划", example = "2026 年度绩效考核")
    private String latestPlanName;

    @Schema(description = "最近绩效评分", example = "95.50")
    private BigDecimal latestScore;

    @Schema(description = "最近绩效等级", example = "A")
    private String latestResultLevel;

    @Schema(description = "考核次数", example = "3")
    private Long assessmentCount;

}
