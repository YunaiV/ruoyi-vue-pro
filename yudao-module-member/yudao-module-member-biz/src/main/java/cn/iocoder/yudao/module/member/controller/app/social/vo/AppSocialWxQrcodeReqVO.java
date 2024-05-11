package cn.iocoder.yudao.module.member.controller.app.social.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 获得获取小程序码 Request VO")
@Data
public class AppSocialWxQrcodeReqVO {

    private static String SCENE = "1011"; // 默认场景值 1011 扫描二维码
    private static String ENV_VERSION = "develop"; // 小程序版本。正式版为 "release"，体验版为 "trial"，开发版为 "develop"
    private static Integer WIDTH = 430; // 二维码宽度
    private static Boolean AUTO_COLOR = true; // 默认true 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
    private static Boolean CHECK_PATH = true; // 默认true 检查 page 是否存在
    private static Boolean IS_HYALINE = true; // 是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码

    /**
     * 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~， 其它字符请自行编码为合法字符
     * （因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
     */
    @Schema(description = "场景值/页面参数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private String scene = SCENE;
    /**
     * 页面路径
     */
    @Schema(description = "页面路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "pages/goods/index")
    @NotEmpty(message = "页面路径不能为空")
    private String path;
    /**
     * 要打开的小程序版本。默认是开发版。
     */
    @Schema(description = "小程序版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "develop")
    private String envVersion = ENV_VERSION;
    /**
     * 二维码宽度
     */
    @Schema(description = "二维码宽度", requiredMode = Schema.RequiredMode.REQUIRED, example = "430")
    private Integer width = WIDTH;
    /**
     * 默认true 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
     */
    @Schema(description = "是/否自动配置线条颜色", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean isAutoColor = AUTO_COLOR;
    /**
     * 默认true 检查 page 是否存在，为 true 时 page 必须是已经发布的小程序存在的页面（否则报错）；
     * 为 false 时允许小程序未发布或者 page 不存在，但 page 有数量上限（60000个）请勿滥用
     */
    @Schema(description = "是/否检查 page 是否存在", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean isCheckPath = CHECK_PATH;
    /**
     * 是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码
     */
    @Schema(description = "是/否需要透明底色", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean isHyaline = IS_HYALINE;

    @Schema(description = "社交平台的类型，参见 SocialTypeEnum 枚举值", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @InEnum(SocialTypeEnum.class)
    @NotNull(message = "社交平台的类型不能为空")
    private Integer type;

}
