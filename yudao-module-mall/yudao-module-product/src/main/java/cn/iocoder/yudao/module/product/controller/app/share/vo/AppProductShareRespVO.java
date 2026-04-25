package cn.iocoder.yudao.module.product.controller.app.share.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "用户 App - 分享链接 Response VO")
@Data
public class AppProductShareRespVO {

    @Schema(description = "分享令牌")
    private String token;

    @Schema(description = "分享链接 URL")
    private String shareUrl;

    @Schema(description = "二维码参数（前端渲染）")
    private String qrCodeParam;

    @Schema(description = "过期时间（ISO）")
    private String expiresAt;

    @Schema(description = "平台专属内容（title/description/hashtags 等）")
    private Map<String, Object> platformContent;

    @Schema(description = "分享按钮配置（微信/微博/邮件）")
    private Map<String, Object> shareButtons;

    @Schema(description = "最佳发布时段建议")
    private String bestTimeToPost;

    @Schema(description = "访问统计（getPerformance 接口返回）")
    private Map<String, Object> performance;

}
