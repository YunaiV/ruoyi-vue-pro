package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 站内信模版更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyTemplateUpdateReqVO extends NotifyTemplateBaseVO {

    @ApiModelProperty(value = "ID", required = true, example = "1024")
    @NotNull(message = "ID 不能为空")
    private Long id;

}
