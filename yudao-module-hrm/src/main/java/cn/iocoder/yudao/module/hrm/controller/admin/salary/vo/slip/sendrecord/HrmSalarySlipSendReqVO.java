package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.template.HrmSalarySlipTemplateOptionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 工资条发放 Request VO")
@Data
public class HrmSalarySlipSendReqVO {

    @Schema(description = "工资表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "工资表编号不能为空")
    private Long monthRecordId;

    @Schema(description = "是否隐藏空值项", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否隐藏空值项不能为空")
    private Boolean hideEmpty;

    @Schema(description = "工资条模板项列表")
    @Valid
    private List<HrmSalarySlipTemplateOptionVO> options;

    @Schema(description = "是否发放全部筛选结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否发放全部筛选结果不能为空")
    private Boolean all;

    @Schema(description = "员工编号列表", example = "[1024, 1025]")
    private List<Long> employeeIds;

    @Schema(description = "员工姓名、工号或手机号", example = "张三")
    private String search;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "是否已发送", example = "false")
    private Boolean sent;

    @AssertTrue(message = "发放已选员工时，员工不能为空")
    @JsonIgnore
    public boolean isEmployeeIdsValid() {
        return Boolean.TRUE.equals(all) || CollUtil.isNotEmpty(employeeIds);
    }

}
