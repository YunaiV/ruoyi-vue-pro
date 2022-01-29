package cn.iocoder.yudao.module.system.controller.admin.sms.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("管理后台 - 短信模板的发送 Request VO")
@Data
public class SmsTemplateSendReqVO {

    @ApiModelProperty(value = "手机号", required = true, example = "15601691300")
    @NotNull(message = "手机号不能为空")
    private String mobile;

    @ApiModelProperty(value = "模板编码", required = true, example = "test_01")
    @NotNull(message = "模板编码不能为空")
    private String templateCode;

    @ApiModelProperty(value = "模板参数")
    private Map<String, Object> templateParams;

}
