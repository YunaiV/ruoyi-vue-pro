package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - HRM 绩效计划员工分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmPerformanceAssessmentPageReqVO extends PageParam {

    @Schema(description = "绩效计划编号", example = "1024")
    private Long planId;

    @Schema(description = "员工编号", example = "2048")
    private Long employeeId;

    @Schema(description = "员工编号列表", example = "[2048, 2049]")
    private List<Long> employeeIds;

    @Schema(description = "员工姓名或工号", example = "张三")
    private String search;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "聘用形式", example = "1")
    private Integer employeeType;

    @Schema(description = "员工状态", example = "1")
    private Integer employeeStatus;

    @Schema(description = "考核状态", example = "1")
    @InEnum(value = HrmPerformancePlanStatusEnum.class, message = "考核状态必须是 {value}")
    private Integer status;

    @Schema(description = "阶段状态", example = "2")
    @InEnum(value = HrmPerformanceStageTypeEnum.class, message = "阶段状态必须是 {value}")
    private Integer stageType;

    @Schema(description = "申诉状态", example = "1")
    @InEnum(value = HrmPerformanceAppealStatusEnum.class, message = "申诉状态必须是 {value}")
    private Integer appealStatus;

    @Schema(description = "结果等级", example = "A")
    private String resultLevel;

    @Schema(description = "结果等级是否为空", example = "false")
    private Boolean resultLevelEmpty;

}
