package cn.iocoder.yudao.module.ai1.controller.admin.memory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI1 长期记忆修改 Request VO")
@Data
public class Ai1MemoryUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "记忆正文", requiredMode = Schema.RequiredMode.REQUIRED, example = "## 用户画像\n- 后端工程师")
    @NotBlank(message = "记忆正文不能为空")
    @Size(max = 65535, message = "记忆正文长度不能超过 65535 个字符")
    private String content;

}
