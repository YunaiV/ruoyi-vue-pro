package cn.iocoder.yudao.userserver.modules.system.controller.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("手机密码登录 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysAuthLoginRespVO {

    @ApiModelProperty(value = "token", required = true, example = "yudaoyuanma")
    private String token;

}
