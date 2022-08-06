package cn.iocoder.yudao.module.system.controller.admin.notify.vo.message;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 站内信更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyMessageUpdateReqVO extends NotifyMessageBaseVO {

    @ApiModelProperty(value = "ID", required = true)
    @NotNull(message = "ID不能为空")
    private Long id;

    @ApiModelProperty(value = "站内信模版编号")
    private Long templateId;

}
