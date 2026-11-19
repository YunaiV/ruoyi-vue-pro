package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAspectRatioEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStyleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧项目创建 / 修改 Request VO")
@Data
public class AiDramaProjectSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "末班地铁")
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 64, message = "项目名称不能超过 64 个字符")
    private String name;

    @Schema(description = "项目简介", example = "都市悬疑短剧")
    @Size(max = 500, message = "项目简介不能超过 500 个字符")
    private String description;

    @Schema(description = "故事想法", example = "末班地铁上，女孩发现对面的乘客是十年后的自己")
    @Size(max = 5000, message = "故事想法不能超过 5000 个字符")
    private String idea;

    @Schema(description = "画风", example = "2")
    @InEnum(AiDramaStyleEnum.class)
    private Integer style;

    @Schema(description = "美学技能编号：资产出图使用", example = "1")
    private Long aestheticSkillId;

    @Schema(description = "导演技能编号：拆分分镜使用", example = "2")
    private Long directorSkillId;

    @Schema(description = "分镜技能编号：拆分分镜时按顺序追加", example = "[3, 4]")
    private List<Long> storyboardSkillIds;

    @Schema(description = "画幅", example = "9:16")
    @InEnum(AiDramaAspectRatioEnum.class)
    private String composeAspectRatio;

}
