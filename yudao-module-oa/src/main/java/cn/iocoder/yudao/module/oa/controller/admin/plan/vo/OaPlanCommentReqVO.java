package cn.iocoder.yudao.module.oa.controller.admin.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - OA 工作计划点评 Request VO")
@Data
public class OaPlanCommentReqVO {

    @Schema(description = "计划编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "计划编号不能为空")
    private Long id;

    @Schema(description = "计划点评", requiredMode = Schema.RequiredMode.REQUIRED, example = "计划执行及时，后续继续跟进")
    @NotBlank(message = "计划点评不能为空")
    @Size(max = 1000, message = "点评内容长度不能超过 {max} 个字符")
    private String comment;

}
