package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 工资条模板 Response VO")
@Data
public class HrmSalarySlipTemplateRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "是否隐藏空值项")
    private Boolean hideEmpty;

    @Schema(description = "是否默认选项")
    private Boolean defaultStatus;

    @Schema(description = "选项列表")
    private List<HrmSalarySlipTemplateOptionVO> options;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
