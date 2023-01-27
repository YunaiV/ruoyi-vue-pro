package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("管理后台 - 邮件发送 Req VO")
@Data
public class MailTemplateSendReqVO {

    @ApiModelProperty(value = "接收邮箱", required = true, example = "7685413@qq.com")
    @NotEmpty(message = "接收邮箱不能为空")
    private String mail;

    @ApiModelProperty(value = "模板编码", required = true, example = "test_01")
    @NotNull(message = "模板编码不能为空")
    private String templateCode;

    @ApiModelProperty(value = "模板参数")
    private Map<String, Object> templateParams;

}
