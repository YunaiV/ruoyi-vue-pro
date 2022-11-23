package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("管理后台 - 站内信模板的发送 Request VO")
@Data
public class NotifyTemplateSendReqVO {

    @ApiModelProperty(value = "用户id", required = true, example = "01")
    @NotNull(message = "用户id不能为空")
    private Long userId;

    @ApiModelProperty(value = "模板Id", required = true, example = "01")
    @NotNull(message = "模板Id不能为空")
    private Long templateId;

    @ApiModelProperty(value = "模板参数")
    private Map<String, Object> templateParams;
}
