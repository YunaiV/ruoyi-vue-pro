package cn.iocoder.yudao.module.mp.controller.admin.message.vo.autoreply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 公众号自动回复的创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpAutoReplyCreateReqVO extends MpAutoReplyBaseVO {

    @Schema(description = "公众号账号的编号", required = true, example = "1024")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

}
