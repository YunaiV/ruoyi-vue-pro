package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 绩效计划分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmPerformancePlanPageReqVO extends PageParam {

    @Schema(description = "绩效计划名称", example = "2026 年第三季度绩效计划")
    private String name;

    @Schema(description = "绩效计划状态", example = "1")
    @InEnum(value = HrmPerformancePlanStatusEnum.class, message = "计划状态必须是 {value}")
    private Integer status;

    @Schema(description = "阶段状态", example = "2")
    @InEnum(value = HrmPerformanceStageTypeEnum.class, message = "阶段状态必须是 {value}")
    private Integer stageType;

}
