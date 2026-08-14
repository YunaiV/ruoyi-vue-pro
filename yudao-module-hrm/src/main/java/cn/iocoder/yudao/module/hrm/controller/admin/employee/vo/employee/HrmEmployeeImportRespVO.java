package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - HRM 员工档案导入 Response VO")
@Data
@Builder
public class HrmEmployeeImportRespVO {

    @Schema(description = "创建成功的工号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createJobNumbers;

    @Schema(description = "更新成功的工号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateJobNumbers;

    @Schema(description = "因重复而跳过的工号数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> skipJobNumbers;

    @Schema(description = "导入失败的员工集合，key 为原始行号和员工标识，value 为失败原因",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureJobNumbers;

}
