package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - HRM 员工薪资信息导入 Response VO")
@Data
@Builder
public class HrmSalaryEmployeeInfoImportRespVO {

    @Schema(description = "导入成功的工号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> successJobNumbers;

    @Schema(description = "导入失败的员工集合，key 为工号或行号，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureJobNumbers;

}
