package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - AI 短剧剧本打磨 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AiDramaScriptPolishReqVO extends AiDramaChatReqVO {

    @Schema(description = "打磨意见", requiredMode = Schema.RequiredMode.REQUIRED, example = "结尾加一个反转")
    @NotBlank(message = "打磨意见不能为空")
    @Size(max = 1000, message = "打磨意见不能超过 1000 个字符")
    private String note;

}
