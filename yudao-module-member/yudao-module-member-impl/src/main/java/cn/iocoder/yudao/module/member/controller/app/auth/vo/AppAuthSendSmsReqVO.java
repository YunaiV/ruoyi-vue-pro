package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.member.enums.sms.SysSmsSceneEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@ApiModel("APP 端 - 发送手机验证码 Response VO")
@Data
@Accessors(chain = true)
public class AppAuthSendSmsReqVO {

    @ApiModelProperty(value = "手机号", example = "15601691234")
    @Mobile
    private String mobile;

    @ApiModelProperty(value = "发送场景", example = "1", notes = "对应 MbrSmsSceneEnum 枚举")
    @NotNull(message = "发送场景不能为空")
    @InEnum(SysSmsSceneEnum.class)
    private Integer scene;

}
