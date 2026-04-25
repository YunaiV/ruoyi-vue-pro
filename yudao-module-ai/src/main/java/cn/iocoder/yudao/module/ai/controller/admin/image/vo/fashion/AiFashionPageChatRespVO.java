package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - AI 悬浮对话框（页面级）Response VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - AI 悬浮对话框页面聊天 Response VO")
@Data
public class AiFashionPageChatRespVO {

    @Schema(description = "会话令牌", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionToken;

    @Schema(description = "当前页面", example = "model_library")
    private String pageName;

    @Schema(description = "AI 回复文本", example = "为您找到5位高挑苗条的模特，请查看下方结果")
    private String reply;

    @Schema(description = "页面相关行动（前端可按此跳转/操作）", example = "[\"FILTER_MODEL_BODY_TYPE:slim\",\"FILTER_MODEL_HEIGHT_MIN:175\"]")
    private List<String> actions;

    @Schema(description = "关联素材 ID 列表（如搜到的模特/图片）")
    private List<Long> relatedImageIds;

    @Schema(description = "关联设计任务 ID（若触发了设计任务）")
    private Long relatedTaskId;

    @Schema(description = "关联 3D 资产 ID（若触发了 3D 操作）")
    private Long relatedAssetId;

    @Schema(description = "悬浮窗是否最小化", example = "false")
    private Boolean minimized;

    @Schema(description = "悬浮窗 X 坐标", example = "20")
    private Integer positionX;

    @Schema(description = "悬浮窗 Y 坐标", example = "20")
    private Integer positionY;

}
