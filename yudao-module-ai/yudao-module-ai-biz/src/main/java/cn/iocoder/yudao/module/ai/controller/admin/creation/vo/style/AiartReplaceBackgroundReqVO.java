package cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 商品替换背景请求 VO")
@Data
public class AiartReplaceBackgroundReqVO {

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "画一个长城")
    @NotEmpty(message = "提示词不能为空")
    @Size(max = 1200, message = "提示词最大 1200")
    private String prompt;

}
