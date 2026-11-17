package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character;

import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.AiDramaAudioRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧角色 Response VO")
@Data
public class AiDramaCharacterRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "林夏")
    private String name;

    @Schema(description = "别名、称呼，多个以逗号分隔", example = "小夏,林司机")
    private String aliases;

    @Schema(description = "角色层级", example = "S")
    private String roleLevel;

    @Schema(description = "性别", example = "2")
    private Integer sex;

    @Schema(description = "年龄段", example = "25 岁左右")
    private String ageRange;

    @Schema(description = "性格标签")
    private List<String> personalityTags;

    @Schema(description = "服装华丽度", example = "2")
    private Integer costumeTier;

    @Schema(description = "角色设定", example = "25 岁")
    private String description;

    @Schema(description = "角色出图提示词", example = "短发女孩")
    private String prompt;

    @Schema(description = "角色参考图", example = "https://www.iocoder.cn/1.png")
    private String referenceImageUrl;

    @Schema(description = "声音描述", example = "年轻女声，语速偏快")
    private String voiceDescription;

    @Schema(description = "样音文本", example = "这是最后一班车了。")
    private String sampleText;

    @Schema(description = "选用的样音编号", example = "1")
    private Long sampleAudioId;

    @Schema(description = "形象版本")
    private List<AiDramaAppearanceRespVO> appearances;

    @Schema(description = "样音")
    private List<AiDramaAudioRespVO> audios;

}
