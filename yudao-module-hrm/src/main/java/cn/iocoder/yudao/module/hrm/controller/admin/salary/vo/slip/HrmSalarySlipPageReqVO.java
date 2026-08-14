package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - HRM 工资条分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmSalarySlipPageReqVO extends PageParam {
    @Schema(description = "工资条记录编号", example = "1024")
    private Long sendRecordId;
    @Schema(description = "员工编号", example = "2048")
    private Long employeeId;
    @Schema(description = "员工编号列表", example = "[2048, 2049]")
    private List<Long> employeeIds;
    @Schema(description = "搜索关键字", example = "张三")
    private String search;
    @Schema(description = "部门编号", example = "100")
    private Long deptId;
    @Schema(description = "已读状态", example = "1")
    private Integer readStatus;
    @Schema(description = "备注", example = "7 月工资条")
    private String remark;
}
