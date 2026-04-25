package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - AI 服装设计智能对话 Response VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - AI 服装设计智能对话 Response VO")
@Data
public class AiFashionSmartChatRespVO {

    @Schema(description = "会话令牌（首次请求时由服务端生成，后续请求传回）", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionToken;

    @Schema(description = "系统对用户输入的理解描述", example = "已理解：批量生成5款甜酷风连衣裙")
    private String interpretation;

    @Schema(description = "解析到的意图类型", example = "BATCH_GENERATE")
    private String intent;

    @Schema(description = "本次生成数量", example = "5")
    private Integer batchCount;

    @Schema(description = "单款任务ID（batchCount=1 时有值）", example = "1024")
    private Long taskId;

    @Schema(description = "批量任务ID列表（batchCount>1 时有值）")
    private List<Long> taskIds;

    @Schema(description = "检测到的颜色 SD 关键词", example = "red, vibrant red")
    private String detectedColor;

    @Schema(description = "检测到的颜色 Hex 值（供前端显示）", example = "#FF0000")
    private String detectedColorHex;

    @Schema(description = "检测到的风格 SD 关键词", example = "sweet edgy style, Y2K aesthetic")
    private String detectedStyle;

    @Schema(description = "检测到的面料 SD 关键词", example = "denim fabric, denim material")
    private String detectedFabric;

    @Schema(description = "修改项列表（MODIFY 类意图时填充）", example = "[\"颜色→红色\", \"面料→牛仔\"]")
    private List<String> modifications;

    @Schema(description = "工作流模式", example = "PROFESSIONAL")
    private String workflowMode;

    @Schema(description = "质量预设", example = "HIGH")
    private String qualityPreset;

    @Schema(description = "自动生成的完整 SD 提示词", example = "sweet edgy style, dress, red, professional fashion photography")
    private String enhancedPrompt;

}
