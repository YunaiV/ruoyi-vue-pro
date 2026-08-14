package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工薪资信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmSalaryEmployeeInfoPageReqVO extends PageParam {

    @Schema(description = "员工编号", example = "1024")
    private Long employeeId;

    @Schema(description = "员工编号列表", example = "[1024, 1025]")
    private List<Long> employeeIds;

    @Schema(description = "调整类型", example = "1")
    private Integer changeType;

    @Schema(description = "员工姓名或工号", example = "张三")
    private String search;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "岗位名称", example = "Java 工程师")
    private String postName;

    @Schema(description = "员工状态", example = "1")
    private Integer status;

    @Schema(description = "入职状态", example = "1")
    private Integer entryStatus;

    @Schema(description = "员工状态页签", example = "1")
    private Integer statusCategory;

}
