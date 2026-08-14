package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - HRM 员工字段配置值 VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeFieldConfigValueVO {

    @Schema(description = "字段名称", example = "mobile")
    private String name;

    @Schema(description = "是否显示", example = "true")
    private Boolean visible;

    @Schema(description = "是否允许员工编辑", example = "true")
    private Boolean editable;

}
