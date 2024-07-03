package cn.iocoder.yudao.module.ai.controller.admin.write.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - AI 写作生成 Request VO")
@Data
public class AiWriteGenerateReqVO {

    @Schema(description = "写作内容提示", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "田忌赛马")
    private String contentPrompt;

    @Schema(description = "原文", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "领导我要辞职")
    private String originalContent;

    @Schema(description = "回复内容", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "准了")
    private String replyContentPrompt;

    @Schema(description = "长度", requiredMode = Schema.RequiredMode.REQUIRED, example = "中等")
    @NotBlank(message = "长度不能为空")
    private String length;

    @Schema(description = "格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "文章")
    @NotBlank(message = "格式不能为空")
    private String format;

    @Schema(description = "语气", requiredMode = Schema.RequiredMode.REQUIRED, example = "随意")
    @NotBlank(message = "语气不能为空")
    private String tone;

    @Schema(description = "语言", requiredMode = Schema.RequiredMode.REQUIRED, example = "中文")
    @NotBlank(message = "语言不能为空")
    private String language;


    @Schema(description = "写作类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer writeType; //参见 AiWriteTypeEnum 枚举
}