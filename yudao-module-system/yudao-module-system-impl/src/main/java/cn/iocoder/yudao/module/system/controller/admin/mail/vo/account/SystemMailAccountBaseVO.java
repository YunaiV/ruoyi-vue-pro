package cn.iocoder.yudao.module.system.controller.admin.mail.vo.account;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

// TODO @ジョイイ：System 去掉哈
@Data
public class SystemMailAccountBaseVO {

    // TODO @ジョイイ：example 写的不太对，这个应该是邮箱；
    @ApiModelProperty(value = "来源" , required = true , example = "yudaoyuanma")
    private String from;

    @ApiModelProperty(value = "用户名" , required = true , example = "yudao")
    private String username;

    @ApiModelProperty(value = "密码" , required = true , example = "123456")
    private String password;

    @ApiModelProperty(value = "网站" , required = true , example = "www.iocoder.cn")
    private String host;

    @ApiModelProperty(value = "端口" , required = true , example = "80")
    private String port;

    // TODO @ジョイイ：Boolean
    @ApiModelProperty(value = "是否开启ssl" , required = true , example = "2")
    private Integer sslEnable;
}
