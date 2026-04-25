package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理后台 - AI 悬浮对话框（页面级）Request VO
 *
 * <p>适用于首页、模特库、设计工作室、图库、3D预览等页面的浮动对话框。</p>
 *
 * @author deepay
 */
@Schema(description = "管理后台 - AI 悬浮对话框页面聊天 Request VO")
@Data
public class AiFashionPageChatReqVO {

    @Schema(description = "会话令牌（首次为空）", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionToken;

    @Schema(description = "当前页面名称（home/model_library/design_studio/image_library/3d_viewer）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "model_library")
    @NotBlank(message = "页面名称不能为空")
    private String pageName;

    @Schema(description = "用户消息", requiredMode = Schema.RequiredMode.REQUIRED, example = "帮我找一个高挑苗条的模特")
    @NotBlank(message = "消息内容不能为空")
    private String message;

    @Schema(description = "是否最小化悬浮窗", example = "false")
    private Boolean minimized;

    @Schema(description = "悬浮窗 X 坐标", example = "20")
    private Integer positionX;

    @Schema(description = "悬浮窗 Y 坐标", example = "20")
    private Integer positionY;

}
