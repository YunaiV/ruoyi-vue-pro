package cn.iocoder.yudao.module.member.controller.app.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;


@Schema(description = "用户 APP - 获得获取小程序码 Request VO")
@Data
public class AppSocialWxaQrcodeReqVO {

    /**
     * 页面路径不能携带参数（参数请放在scene字段里）
     */
    @Schema(description = "场景值", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private String scene;

    /**
     * 默认是主页，页面 page，例如 pages/index/index，根路径前不要填加 /，不能携带参数（参数请放在scene字段里），
     * 如果不填写这个字段，默认跳主页面。scancode_time为系统保留参数，不允许配置
     */
    @Schema(description = "页面路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "pages/goods/index")
    @NotEmpty(message = "页面路径不能为空")
    private String path;

    @Schema(description = "二维码宽度", requiredMode = Schema.RequiredMode.REQUIRED, example = "430")
    private Integer width;

    @Schema(description = "是/否自动配置线条颜色", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean autoColor;

    @Schema(description = "是/否检查 page 是否存在", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean checkPath;

    @Schema(description = "是/否需要透明底色", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean hyaline;

}
