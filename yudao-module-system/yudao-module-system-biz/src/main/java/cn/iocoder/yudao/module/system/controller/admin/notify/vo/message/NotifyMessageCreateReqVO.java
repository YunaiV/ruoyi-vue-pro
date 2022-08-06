package cn.iocoder.yudao.module.system.controller.admin.notify.vo.message;

import lombok.*;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 站内信创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyMessageCreateReqVO extends NotifyMessageBaseVO {

    @ApiModelProperty(value = "站内信模版编号")
    private Long templateId;

}
