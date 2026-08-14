package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 薪资组分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmSalaryGroupPageReqVO extends PageParam {

    @Schema(description = "薪资组名称", example = "总部薪资组")
    private String name;

    @Schema(description = "计税规则编号", example = "1024")
    private Long taxRuleId;

}
