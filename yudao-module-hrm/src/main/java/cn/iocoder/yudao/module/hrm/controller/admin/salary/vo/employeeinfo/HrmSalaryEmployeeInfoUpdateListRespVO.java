package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - HRM 批量调薪 Response VO")
@Data
public class HrmSalaryEmployeeInfoUpdateListRespVO {

    @Schema(description = "成功员工编号列表")
    private List<Long> successEmployeeIds = new ArrayList<>();

    @Schema(description = "失败员工及原因")
    private Map<Long, String> failureEmployeeReasons = new LinkedHashMap<>();

}
