package cn.iocoder.yudao.module.member.controller.app.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Schema(description = "用户 APP - 获得获取小程序码 Request VO")
@Data
public class AppSocialWxQrcodeReqVO {

    // TODO @puhui999: 没有默认值 getQrcodeService().createWxaCodeUnlimitBytes() 转类型会报错 🤣
    public static String ENV_VERSION = "release"; // 小程序版本。正式版为 "release"，体验版为 "trial"，开发版为 "develop"
    private static String SCENE = ""; // 页面路径不能携带参数（参数请放在scene字段里）
    private static Integer WIDTH = 430; // 二维码宽度
    private static Boolean AUTO_COLOR = true; // 默认true 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
    private static Boolean CHECK_PATH = true; // 默认true 检查 page 是否存在
    private static Boolean HYALINE = true; // 是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码

    /**
     * 页面路径不能携带参数（参数请放在scene字段里）
     */
    @Schema(description = "场景值", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private String scene = SCENE;

    /**
     * 默认是主页，页面 page，例如 pages/index/index，根路径前不要填加 /，不能携带参数（参数请放在scene字段里），
     * 如果不填写这个字段，默认跳主页面。scancode_time为系统保留参数，不允许配置
     */
    @Schema(description = "页面路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "pages/goods/index")
    @NotEmpty(message = "页面路径不能为空")
    private String path;

    @Schema(description = "二维码宽度", requiredMode = Schema.RequiredMode.REQUIRED, example = "430")
    private Integer width = WIDTH;

    @Schema(description = "是/否自动配置线条颜色", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean autoColor = AUTO_COLOR;

    @Schema(description = "是/否检查 page 是否存在", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean checkPath = CHECK_PATH;

    @Schema(description = "是/否需要透明底色", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean hyaline = HYALINE;

}
