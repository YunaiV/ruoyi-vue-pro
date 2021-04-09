package cn.iocoder.dashboard.modules.system.controller.sms.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("短信模板的发送 Request VO")
@Data
public class SysSmsTemplateSendReqVO {

    @ApiModelProperty(value = "模板编码", required = true, example = "test_01")
    @NotNull(message = "模板编码不能为空")
    private String code;

    @ApiModelProperty(value = "模板参数")
    private Map<String, Object> params;

}
