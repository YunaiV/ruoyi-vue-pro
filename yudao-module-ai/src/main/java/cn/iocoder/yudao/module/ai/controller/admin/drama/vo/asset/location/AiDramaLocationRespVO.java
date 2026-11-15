package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location;

import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaCharacterAppearanceDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧场景 Response VO")
@Data
public class AiDramaLocationRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "末班地铁车厢")
    private String name;

    @Schema(description = "场景设定", example = "深夜")
    private String description;

    @Schema(description = "场景出图提示词", example = "深夜地铁车厢")
    private String prompt;

    @Schema(description = "候选图")
    private List<AiDramaCharacterAppearanceDO.Candidate> candidates;

    @Schema(description = "选用的候选下标", example = "0")
    private Integer selectedIndex;

    @Schema(description = "用户参考图", example = "https://www.iocoder.cn/1.png")
    private String referenceImageUrl;

    @Schema(description = "最近一次生成的状态，参见 AiDramaStatusEnum 枚举类", example = "40")
    private Integer status;

    @Schema(description = "生成失败原因", example = "生成失败")
    private String errorMessage;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
