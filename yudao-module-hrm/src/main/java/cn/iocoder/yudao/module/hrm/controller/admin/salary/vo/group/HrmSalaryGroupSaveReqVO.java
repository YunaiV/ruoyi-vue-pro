package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 薪资组保存 Request VO")
@Data
public class HrmSalaryGroupSaveReqVO {

    @Schema(description = "薪资组编号", example = "1024")
    private Long id;

    @Schema(description = "薪资组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "总部薪资组")
    @NotBlank(message = "薪资组名称不能为空")
    @Size(max = 64, message = "薪资组名称不能超过 64 个字符")
    private String name;

    @Schema(description = "计税规则编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "计税规则不能为空")
    private Long taxRuleId;

    @Schema(description = "适用部门编号列表", example = "[100, 101]")
    private List<Long> deptIds;

    @Schema(description = "适用员工编号列表", example = "[1024, 1025]")
    private List<Long> employeeIds;

    @AssertTrue(message = "适用部门和适用员工不能同时为空")
    @JsonIgnore
    public boolean isScopeValid() {
        return CollUtil.isNotEmpty(deptIds) || CollUtil.isNotEmpty(employeeIds);
    }

}
