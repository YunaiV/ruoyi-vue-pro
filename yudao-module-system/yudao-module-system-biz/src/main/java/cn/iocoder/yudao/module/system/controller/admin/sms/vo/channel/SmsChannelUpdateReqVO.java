package cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 短信渠道更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsChannelUpdateReqVO extends SmsChannelBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

}
