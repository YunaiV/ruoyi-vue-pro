package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.changetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 调薪模板 Response VO")
@Data
public class HrmSalaryChangeTemplateRespVO {

    @Schema(description = "调薪模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发调薪模板")
    private String name;

    @Schema(description = "是否默认模板", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean defaultStatus;

    @Schema(description = "调薪项配置")
    private List<HrmSalaryChangeOptionVO> options;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
