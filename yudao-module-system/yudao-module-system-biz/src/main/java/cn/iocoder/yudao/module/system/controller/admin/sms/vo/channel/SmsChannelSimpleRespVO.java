package cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 短信渠道精简 Response VO")
@Data
public class SmsChannelSimpleRespVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @ApiModelProperty(value = "短信签名", required = true, example = "芋道源码")
    @NotNull(message = "短信签名不能为空")
    private String signature;

    @ApiModelProperty(value = "渠道编码", required = true, example = "YUN_PIAN", notes = "参见 SmsChannelEnum 枚举类")
    private String code;

}
