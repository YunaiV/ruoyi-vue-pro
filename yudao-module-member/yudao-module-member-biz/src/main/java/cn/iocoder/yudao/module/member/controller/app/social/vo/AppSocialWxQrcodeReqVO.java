package cn.iocoder.yudao.module.member.controller.app.social.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// TODO @芋艿：需要精简下参数；
@Schema(description = "用户 APP - 获得获取小程序码 Request VO")
@Data
public class AppSocialWxQrcodeReqVO {

    // TODO @puhui999：这个后续不用前端传递，应该是后端搞的
    private static String SCENE = "1011"; // 默认场景值 1011 扫描二维码
    // TODO @puhui999：这个默认是不是 release 哈？
    private static String ENV_VERSION = "develop"; // 小程序版本。正式版为 "release"，体验版为 "trial"，开发版为 "develop"
    // TODO @puhui999：这个去掉；因为本身就是 430 啦；
    private static Integer WIDTH = 430; // 二维码宽度
    // TODO @puhui999：这个去掉；因为本身就是 true 啦；
    private static Boolean AUTO_COLOR = true; // 默认true 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
    // TODO @puhui999：这个去掉；因为本身就是 true 啦；
    private static Boolean CHECK_PATH = true; // 默认true 检查 page 是否存在
    // TODO @puhui999：这个去掉；因为本身就是 true 啦；
    private static Boolean IS_HYALINE = true; // 是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码

    @Schema(description = "场景值", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private String scene = SCENE;

    @Schema(description = "页面路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "pages/goods/index")
    @NotEmpty(message = "页面路径不能为空")
    private String path;

    // TODO @puhui999：这个应该不传递哈
    @Schema(description = "小程序版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "develop")
    private String envVersion = ENV_VERSION;

    @Schema(description = "二维码宽度", requiredMode = Schema.RequiredMode.REQUIRED, example = "430")
    private Integer width = WIDTH;

    @Schema(description = "是/否自动配置线条颜色", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean isAutoColor = AUTO_COLOR;

    @Schema(description = "是/否检查 page 是否存在", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean isCheckPath = CHECK_PATH;

    @Schema(description = "是/否需要透明底色", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean isHyaline = IS_HYALINE;

    @Schema(description = "社交平台的类型，参见 SocialTypeEnum 枚举值", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @InEnum(SocialTypeEnum.class)
    @NotNull(message = "社交平台的类型不能为空")
    private Integer type;

}
