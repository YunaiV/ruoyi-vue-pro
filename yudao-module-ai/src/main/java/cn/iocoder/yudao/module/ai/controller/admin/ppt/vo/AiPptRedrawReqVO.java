package cn.iocoder.yudao.module.ai.controller.admin.ppt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI PPT 单页重绘 Request VO
 *
 * 文生图重绘：用修改后的完整提示词重新出图，不参考当前图
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI PPT 单页重绘 Request VO")
@Data
public class AiPptRedrawReqVO {

    @Schema(description = "PPT 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "PPT 编号不能为空")
    private Long id;

    @Schema(description = "页码，从 1 开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "页码不能为空")
    private Integer slideIndex;

    @Schema(description = "完整的生图提示词", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[全局风格：极简商务，蓝白配色] 16:9 幻灯片，左侧大标题文字“市场规模与增速”")
    @NotEmpty(message = "提示词不能为空")
    @Size(max = 4000, message = "提示词不能超过 4000 个字符")
    private String prompt;

}
