package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 绘画生成专业绘画指令 Request VO")
@Data
public class AiImageOptimizePromptReqVO {

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "一只猫在雨后的小巷")
    @NotEmpty(message = "提示词不能为空")
    @Size(max = 1200, message = "提示词最大 1200")
    private String prompt;

}
