package cn.iocoder.yudao.module.ai.controller.admin.ocr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理后台 - AI 文字识别保存修订 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 文字识别保存修订 Request VO")
@Data
public class AiOcrUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "修订后的文本，允许空字符串", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    @NotNull(message = "修订文本不能为空")
    @Size(max = 100000, message = "修订文本不能超过 100000 个字符")
    private String editedText;

}
