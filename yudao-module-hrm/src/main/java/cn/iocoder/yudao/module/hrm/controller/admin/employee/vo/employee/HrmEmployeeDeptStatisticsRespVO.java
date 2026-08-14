package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工部门统计 Response VO")
@Data
@AllArgsConstructor
public class HrmEmployeeDeptStatisticsRespVO {

    @Schema(description = "部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long deptId;

    @Schema(description = "在职员工人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long activeCount;

    @Schema(description = "全职员工人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Long fullTimeCount;

    @Schema(description = "非全职员工人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long nonFullTimeCount;

}
