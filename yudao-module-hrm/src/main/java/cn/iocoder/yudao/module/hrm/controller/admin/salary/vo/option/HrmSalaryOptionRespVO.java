package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 工资表薪资项 Response VO")
@Data
public class HrmSalaryOptionRespVO {

    @Schema(description = "薪资项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "薪资项编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "10101")
    private Integer code;

    @Schema(description = "父薪资项编码", example = "10")
    private Integer parentCode;

    @Schema(description = "薪资项名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "基本工资")
    private String name;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否系统默认项", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean systemFlag;

    @Schema(description = "薪资项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "是否计税", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean taxEnabled;

    @Schema(description = "是否显示", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean visible;

    @Schema(description = "是否参与计算", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean calculateEnabled;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean enabled;

    @Schema(description = "标准薪资项目录编号", example = "1024")
    private Long templateId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
