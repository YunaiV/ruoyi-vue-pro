package cn.iocoder.yudao.module.ai.controller.admin.vo;

import cn.iocoder.yudao.module.ai.enums.AiModelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ai 聊天 req
 *
 * author: fansili
 * time: 2024/3/4 12:33
 */
@Schema(description = "用户 App - 上传文件 Request VO")
@Data
public class AiChatReqVO {

    @Schema(description = "输入内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "输入内容不能为空")
    private String inputText;

    @Schema(description = "AI模型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "AI模型不能为空")
    private AiModelEnum aiModel;

}
