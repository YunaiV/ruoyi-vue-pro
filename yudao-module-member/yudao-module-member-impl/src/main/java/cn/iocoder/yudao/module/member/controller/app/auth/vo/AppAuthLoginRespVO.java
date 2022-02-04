package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("用户 APP - 手机密码登录 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthLoginRespVO {

    @ApiModelProperty(value = "token", required = true, example = "yudaoyuanma")
    private String token;

}
