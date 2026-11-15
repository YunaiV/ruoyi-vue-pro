package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character;

import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaCharacterAppearanceDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧角色形象 Response VO")
@Data
public class AiDramaAppearanceRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "角色编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long characterId;

    @Schema(description = "形象版本序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer appearanceIndex;

    @Schema(description = "形象设定", example = "雨夜")
    private String description;

    @Schema(description = "出图提示词", example = "短发女孩")
    private String prompt;

    @Schema(description = "候选图")
    private List<AiDramaCharacterAppearanceDO.Candidate> candidates;

    @Schema(description = "选用的候选下标", example = "0")
    private Integer selectedIndex;

    @Schema(description = "最近一次生成的状态，参见 AiDramaStatusEnum 枚举类", example = "40")
    private Integer status;

    @Schema(description = "生成失败原因", example = "生成失败")
    private String errorMessage;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
