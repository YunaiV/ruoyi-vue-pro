package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 短剧角色形象 Response VO")
@Data
public class AiDramaAppearanceRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "角色编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long characterId;

    @Schema(description = "形象序号：0 为主形象", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer appearanceIndex;

    @Schema(description = "变化原因", example = "雨夜换装")
    private String changeReason;

    @Schema(description = "形象设定", example = "雨夜")
    private String description;

    @Schema(description = "形象参考图", example = "https://www.iocoder.cn/1.png")
    private String referenceImageUrl;

    @Schema(description = "候选图地址")
    private List<String> imageUrls;

    @Schema(description = "候选图描述：与候选图一一对应")
    private List<String> imageDescriptions;

    @Schema(description = "选用的候选下标", example = "0")
    private Integer selectedIndex;

    @Schema(description = "上一版候选图地址：不为空时可撤销")
    private List<String> previousImageUrls;

    @Schema(description = "绘画模型编号", example = "1")
    private Long modelId;

    @Schema(description = "最近一次生成的状态", example = "40")
    private Integer status;

    @Schema(description = "生成失败原因", example = "生成失败")
    private String errorMessage;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
