package cn.iocoder.yudao.module.hrm.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - HRM HR 工作台统计 Response VO")
@Data
public class HrmHrHomeStatisticsRespVO {

    @Schema(description = "人事概况")
    private EmployeeSurvey employeeSurvey;

    @Schema(description = "招聘动态")
    private RecruitSurvey recruitSurvey;

    @Schema(description = "上月薪资")
    private SalarySurvey salarySurvey;

    @Schema(description = "待办提醒")
    private TodoSurvey todoSurvey;

    @Schema(description = "管理后台 - HRM HR 工作台人事概况 Response VO")
    @Data
    public static class EmployeeSurvey {

        @Schema(description = "在职人数")
        private Long activeCount;

        @Schema(description = "本月入职人数")
        private Long entryThisMonthCount;

        @Schema(description = "本月待入职人数")
        private Long pendingEntryThisMonthCount;

        @Schema(description = "本月离职人数")
        private Long leaveThisMonthCount;

        @Schema(description = "本月待离职人数")
        private Long pendingLeaveThisMonthCount;

        @Schema(description = "本月转正人数")
        private Long regularThisMonthCount;

        @Schema(description = "本月调岗人数")
        private Long transferThisMonthCount;

    }

    @Schema(description = "管理后台 - HRM HR 工作台招聘动态 Response VO")
    @Data
    public static class RecruitSurvey {

        @Schema(description = "招聘中职位数量")
        private Long recruitingPostCount;

        @Schema(description = "招聘流程中候选人数")
        private Long candidateInProcessCount;

        @Schema(description = "待入职人数")
        private Long pendingEntryCount;

        @Schema(description = "已入职人数")
        private Long joinedCount;

    }

    @Schema(description = "管理后台 - HRM HR 工作台上月薪资 Response VO")
    @Data
    public static class SalarySurvey {

        @Schema(description = "工资表编号")
        private Long monthRecordId;

        @Schema(description = "员工数量")
        private Integer employeeCount;

        @Schema(description = "实发工资")
        private BigDecimal realPaySalary;

        @Schema(description = "部门薪资占比列表")
        private List<SalaryDept> deptProportions;

    }

    @Schema(description = "管理后台 - HRM HR 工作台部门薪资占比 Response VO")
    @Data
    public static class SalaryDept {

        @Schema(description = "部门编号")
        private Long deptId;

        @Schema(description = "部门名称")
        private String deptName;

        @Schema(description = "薪资占比")
        private BigDecimal proportion;

        @Schema(description = "实发工资")
        private BigDecimal totalSalary;

    }

    @Schema(description = "管理后台 - HRM HR 工作台待办提醒 Response VO")
    @Data
    public static class TodoSurvey {

        @Schema(description = "待入职人数")
        private Long toEntryCount;

        @Schema(description = "待离职人数")
        private Long toLeaveCount;

        @Schema(description = "合同待到期人数")
        private Long toExpireContractCount;

        @Schema(description = "待转正人数")
        private Long toRegularCount;

        @Schema(description = "待核算工资表数量")
        private Long toSalaryComputeCount;

        @Schema(description = "本月生日员工数量")
        private Long toBirthdayCount;

    }

}
