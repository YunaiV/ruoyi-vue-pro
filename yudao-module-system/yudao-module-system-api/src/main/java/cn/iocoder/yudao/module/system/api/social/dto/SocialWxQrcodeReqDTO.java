package cn.iocoder.yudao.module.system.api.social.dto;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SocialWxQrcodeReqDTO {

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
     * 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~， 其它字符请自行编码为合法字符
     * （因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
     */
    private String scene;
    /**
     * 页面路径
     */
    @NotEmpty(message = "页面路径不能为空")
    private String path;
    /**
     * 要打开的小程序版本。默认是开发版。
     */
    private String envVersion;
    /**
     * 二维码宽度
     */
    private Integer width;
    /**
     * 默认true 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
     */
    private Boolean isAutoColor;
    /**
     * 默认true 检查 page 是否存在，为 true 时 page 必须是已经发布的小程序存在的页面（否则报错）；
     * 为 false 时允许小程序未发布或者 page 不存在，但 page 有数量上限（60000个）请勿滥用
     */
    private Boolean isCheckPath;
    /**
     * 是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码
     */
    private Boolean isHyaline;

}
