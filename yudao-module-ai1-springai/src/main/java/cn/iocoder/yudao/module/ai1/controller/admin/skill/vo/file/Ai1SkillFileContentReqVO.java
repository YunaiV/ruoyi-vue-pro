package cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI1 SKILL 内容文件保存内容 Request VO")
@Data
public class Ai1SkillFileContentReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "文件内容，允许为空字符串", requiredMode = Schema.RequiredMode.REQUIRED, example = "# SKILL")
    @NotNull(message = "文件内容不能为空")
    @Size(max = 16_777_215, message = "内容长度不能超过 16777215 个字符")
    private String content;

}
