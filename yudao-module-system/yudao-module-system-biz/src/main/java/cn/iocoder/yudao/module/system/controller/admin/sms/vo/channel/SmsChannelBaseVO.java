package cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;

/**
* 短信渠道 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class SmsChannelBaseVO {

    @ApiModelProperty(value = "短信签名", required = true, example = "芋道源码")
    @NotNull(message = "短信签名不能为空")
    private String signature;

    @ApiModelProperty(value = "启用状态", required = true, example = "1")
    @NotNull(message = "启用状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "备注", example = "好吃！")
    private String remark;

    @ApiModelProperty(value = "短信 API 的账号", required = true, example = "yudao")
    @NotNull(message = "短信 API 的账号不能为空")
    private String apiKey;

    @ApiModelProperty(value = "短信 API 的密钥", example = "yuanma")
    private String apiSecret;

    @ApiModelProperty(value = "短信发送回调 URL", example = "http://www.iocoder.cn")
    @URL(message = "回调 URL 格式不正确")
    private String callbackUrl;

}
