package cn.iocoder.yudao.module.mp.controller.admin.message.vo.autoreply;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 公众号自动回复的更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpAutoReplyUpdateReqVO extends MpAutoReplyBaseVO {

    @ApiModelProperty(value = "主键", required = true, example = "1024")
    @NotNull(message = "主键不能为空")
    private Long id;

}
