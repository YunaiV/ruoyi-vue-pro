package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.label;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 工作项标签新增/修改 Request VO")
@Data
public class PmsWorkItemLabelSaveReqVO {

    @Schema(description = "标签编号", example = "1024")
    private Long id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "紧急")
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称不能超过 50 个字符")
    private String name;

    @Schema(description = "标签颜色", requiredMode = Schema.RequiredMode.REQUIRED, example = "#409EFF")
    @NotBlank(message = "标签颜色不能为空")
    @Size(max = 20, message = "标签颜色不能超过 20 个字符")
    private String color;

}
