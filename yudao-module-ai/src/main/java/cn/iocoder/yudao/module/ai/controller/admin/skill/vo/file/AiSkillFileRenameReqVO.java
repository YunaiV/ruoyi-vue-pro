package cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 技能文件重命名 Request VO")
@Data
public class AiSkillFileRenameReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "新名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "guide.md")
    @NotEmpty(message = "名称不能为空")
    private String name;

}
