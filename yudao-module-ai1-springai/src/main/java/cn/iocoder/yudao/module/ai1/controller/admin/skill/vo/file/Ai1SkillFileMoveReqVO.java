package cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI1 SKILL 内容文件移动 Request VO")
@Data
public class Ai1SkillFileMoveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "目标父目录编号，0 为根级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "目标父目录编号不能为空")
    private Long parentId;

}
