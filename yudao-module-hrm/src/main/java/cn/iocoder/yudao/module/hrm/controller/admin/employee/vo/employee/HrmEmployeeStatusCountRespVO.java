package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工状态数量 Response VO")
@Data
@AllArgsConstructor
public class HrmEmployeeStatusCountRespVO {

    @Schema(description = "状态页签", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Integer status;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long count;

}
