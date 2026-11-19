package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaCharacterCostumeTierEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaCharacterRoleLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧角色创建 / 修改 Request VO")
@Data
public class AiDramaCharacterSaveReqVO {

    @Schema(description = "编号（修改时必填）", example = "1024")
    private Long id;

    @Schema(description = "项目编号（创建时必填）", example = "1")
    private Long projectId;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "林夏")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称不能超过 64 个字符")
    private String name;

    @Schema(description = "别名、称呼，多个以逗号分隔", example = "小夏,林司机")
    @Size(max = 255, message = "别名不能超过 255 个字符")
    private String aliases;

    @Schema(description = "角色层级", example = "S")
    @InEnum(AiDramaCharacterRoleLevelEnum.class)
    private String roleLevel;

    @Schema(description = "性别", example = "2")
    private Integer sex;

    @Schema(description = "年龄段", example = "25 岁左右")
    @Size(max = 64, message = "年龄段不能超过 64 个字符")
    private String ageRange;

    @Schema(description = "性格标签")
    private List<String> personalityTags;

    @Schema(description = "服装华丽度", example = "2")
    @InEnum(AiDramaCharacterCostumeTierEnum.class)
    private Integer costumeTier;

    @Schema(description = "角色设定", example = "25 岁，地铁司机")
    @Size(max = 2000, message = "角色设定不能超过 2000 个字符")
    private String description;

    @Schema(description = "角色出图提示词", example = "短发女孩，深蓝制服")
    @Size(max = 2000, message = "出图提示词不能超过 2000 个字符")
    private String prompt;

    @Schema(description = "角色参考图", example = "https://www.iocoder.cn/1.png")
    @Size(max = 1024, message = "参考图地址不能超过 1024 个字符")
    private String referenceImageUrl;

    @Schema(description = "声音描述", example = "年轻女声，语速偏快，略带沙哑")
    @Size(max = 500, message = "声音描述不能超过 500 个字符")
    private String voiceDescription;

    @Schema(description = "样音文本", example = "这是最后一班车了。")
    @Size(max = 500, message = "样音文本不能超过 500 个字符")
    private String sampleText;

}
