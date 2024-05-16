package cn.iocoder.yudao.module.system.api.social.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 获取小程序码 Request DTO
 *
 * @author HUIHUI
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/qr-code/getUnlimitedQRCode.html">获取不限制的小程序码</a>
 */
@Data
public class SocialWxQrcodeReqDTO {

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

    /**
     * 是否需要透明底色
     */
    private Boolean autoColor;
    /**
     * 是否检查 page 是否存在
     */
    private Boolean checkPath;
    /**
     * 是否需要透明底色
     */
    private Boolean hyaline;

}
