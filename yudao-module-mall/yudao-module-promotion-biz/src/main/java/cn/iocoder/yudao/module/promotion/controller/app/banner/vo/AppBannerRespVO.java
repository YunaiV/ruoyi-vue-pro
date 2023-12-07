package cn.iocoder.yudao.module.promotion.controller.app.banner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 App - Banner Response VO")
@Data
public class AppBannerRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标题不能为空")
    private String title;

    @Schema(description = "跳转链接", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "跳转链接不能为空")
    private String url;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "图片地址不能为空")
    private String picUrl;

}
