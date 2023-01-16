package cn.iocoder.yudao.module.mp.controller.admin.message.vo.autoreply;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 公众号自动回复的创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpAutoReplyCreateReqVO extends MpAutoReplyBaseVO {

    @ApiModelProperty(value = "微信公众号 ID", required = true, example = "1024")
    @NotNull(message = "微信公众号 ID不能为空")
    private Long accountId;

    @ApiModelProperty(value = "回复类型", required = true, example = "1", notes = "参见 MpAutoReplyTypeEnum 枚举")
    @NotNull(message = "回复类型不能为空")
    private Integer type;

}
