package cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai1.enums.skill.Ai1SkillFileTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI1 SKILL 内容文件新增 Request VO")
@Data
public class Ai1SkillFileCreateReqVO {

    @Schema(description = "SKILL 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "SKILL 编号不能为空")
    private Long skillId;

    @Schema(description = "父目录编号，0 为根级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "父目录编号不能为空")
    private Long parentId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "main.py")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "类型：0-目录、1-文件", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "类型不能为空")
    @InEnum(Ai1SkillFileTypeEnum.class)
    private Integer type;

}
