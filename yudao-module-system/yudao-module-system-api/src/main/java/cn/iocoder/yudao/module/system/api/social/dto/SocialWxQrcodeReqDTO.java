package cn.iocoder.yudao.module.system.api.social.dto;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 获取小程序码 Request DTO
 *
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/qr-code/getUnlimitedQRCode.html">获取不限制的小程序码</a>
 *
 * @author HUIHUI
 */
@Data
public class SocialWxQrcodeReqDTO {

    // TODO @puhui999：userId、userType 应该后续要搞成抽象参数；说白了，就是 path 的参数； socialType 应该去掉，因为就是微信的；
    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /**
     * 用户类型
     */
    @InEnum(UserTypeEnum.class)
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    /**
     * 社交平台的类型
     */
    @InEnum(SocialTypeEnum.class)
    @NotNull(message = "社交平台的类型不能为空")
    private Integer socialType;

    /**
     * 场景
     */
    @NotEmpty(message = "场景不能为空")
    private String scene;
    /**
     * 页面路径
     */
    @NotEmpty(message = "页面路径不能为空")
    private String path;
    /**
     * 要打开的小程序版本
     */
    private String envVersion;
    /**
     * 二维码宽度
     */
    private Integer width;

    // TODO @puhui999：autoColor

    /**
     * 是否需要透明底色
     */
    private Boolean isAutoColor;

    // TODO @puhui999： checkPath
    /**
     * 是否检查 page 是否存在
     */
    private Boolean isCheckPath;

    // TODO @puhui999： hyaline
    /**
     * 是否需要透明底色
     */
    private Boolean isHyaline;

}
