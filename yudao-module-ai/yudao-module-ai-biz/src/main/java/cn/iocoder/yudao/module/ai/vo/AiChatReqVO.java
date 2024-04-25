package cn.iocoder.yudao.module.ai.vo;

import cn.iocoder.yudao.module.ai.enums.AiOpenAiModelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// TODO done @fansili 1）swagger 注释不太对；2）有了 swagger 注释，就不用类注释了
@Data
@Schema(description = "用户 App - 上传文件 Request VO")
public class AiChatReqVO {

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "提示词不能为空!")
    private String prompt;

    @Schema(description = "AI模型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "AI模型不能为空")
    private AiOpenAiModelEnum aiModel;

}
