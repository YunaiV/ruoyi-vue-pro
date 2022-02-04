package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("用户 APP - 取消社交绑定 Request VO，使用 code 授权码")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthSocialUnbindReqVO {

    @ApiModelProperty(value = "社交平台的类型", required = true, example = "10", notes = "参见 SysUserSocialTypeEnum 枚举值")
    @InEnum(SocialTypeEnum.class)
    @NotNull(message = "社交平台的类型不能为空")
    private Integer type;

    @ApiModelProperty(value = "社交的全局编号", required = true, example = "IPRmJ0wvBptiPIlGEZiPewGwiEiE")
    @NotEmpty(message = "社交的全局编号不能为空")
    private String unionId;

}
