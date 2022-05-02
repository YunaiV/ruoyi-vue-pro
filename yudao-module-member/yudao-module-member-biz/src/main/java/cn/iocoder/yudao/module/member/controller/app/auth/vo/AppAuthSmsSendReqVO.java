package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.system.enums.sms.SmsSceneEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@ApiModel("用户 APP - 发送手机验证码 Request VO")
@Data
@Accessors(chain = true)
public class AppAuthSmsSendReqVO {

    @ApiModelProperty(value = "手机号", example = "15601691234")
    @Mobile
    private String mobile;

    @ApiModelProperty(value = "发送场景", example = "1", notes = "对应 SmsSceneEnum 枚举")
    @NotNull(message = "发送场景不能为空")
    @InEnum(SmsSceneEnum.class)
    private Integer scene;

}
