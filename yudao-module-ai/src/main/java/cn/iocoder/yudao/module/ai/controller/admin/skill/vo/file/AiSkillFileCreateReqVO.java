package cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.skill.AiSkillFileTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 技能文件新增 Request VO")
@Data
public class AiSkillFileCreateReqVO {

    @Schema(description = "技能编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "技能编号不能为空")
    private Long skillId;

    @Schema(description = "父目录编号，0 为根级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "父目录编号不能为空")
    private Long parentId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "guide.md")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "类型不能为空")
    @InEnum(AiSkillFileTypeEnum.class)
    private Integer type;

}
