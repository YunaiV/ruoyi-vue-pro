package cn.iocoder.yudao.module.system.api.mail.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@ApiModel("管理后台 - 邮件发送 Req VO")
@Data
public class MailSendReqDTO { // TODO @wangjingqi：1）, 不用空格；2）应该只要传递 templateCode、参数就好，title、from、content、附件应该都是参数里的

    @ApiModelProperty(value = "用户编码",required = true)
    @NotNull(message = "用户编码不能为空")
    private String userId;

    @ApiModelProperty(value = "用户类型",required = true)
    @NotNull(message = "用户类型不能为空")
    private String userType;

    @ApiModelProperty(value = "邮箱模版id",example = "1024")
    @NotNull(message = "邮箱模版编码不能为空")
    private Integer templateCode;

    @ApiModelProperty(value = "邮箱参数")
    @NotNull(message = "模版参数不能为空")
    private Map<String,Object> templateParams;

    @ApiModelProperty(value = "收件人",required = true,example = "yudaoyuanma@123.com")
    @NotNull(message = "收件人不能为空")
    private List<String> tos;



}
