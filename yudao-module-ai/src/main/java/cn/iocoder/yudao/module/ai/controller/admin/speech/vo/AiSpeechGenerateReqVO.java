package cn.iocoder.yudao.module.ai.controller.admin.speech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 语音合成 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 语音合成 Request VO")
@Data
public class AiSpeechGenerateReqVO {

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31")
    @NotNull(message = "模型不能为空")
    private Long modelId;

    @Schema(description = "朗读文本，上限按模型（字符数或 GBK 字节），最多 10000 字符", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "欢迎使用芋道语音合成")
    @NotBlank(message = "朗读文本不能为空")
    @Size(max = 10000, message = "朗读文本不能超过 10000 个字符") // 应用上限：不切分、不拼接
    private String inputText;

    @Schema(description = "音色", requiredMode = Schema.RequiredMode.REQUIRED, example = "male-qn-qingse")
    @NotBlank(message = "音色不能为空")
    @Size(max = 64, message = "音色不能超过 64 个字符")
    private String voice;

    @Schema(description = "音频格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "mp3")
    @NotBlank(message = "音频格式不能为空")
    private String format;

    @Schema(description = "语速倍数，最多两位小数，为空时为 1", example = "1.2")
    private Double speed;

}
