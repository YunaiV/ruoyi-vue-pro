package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工字段配置 Response VO")
@Data
public class HrmEmployeeFieldConfigRespVO {

    @Schema(description = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "name")
    private String name;

    @Schema(description = "字段标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "员工姓名")
    private String title;

    @Schema(description = "字段分组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "基本信息")
    private String groupName;

    @Schema(description = "是否显示", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean visible;

    @Schema(description = "是否允许员工编辑", example = "true")
    private Boolean editable;

    @Schema(description = "是否锁定显示", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean visibleLocked;

    @Schema(description = "是否锁定编辑", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean editableLocked;

}
