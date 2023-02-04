package cn.iocoder.yudao.module.mp.controller.admin.message.vo.autoreply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Schema(description = "管理后台 - 公众号自动回复 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpAutoReplyRespVO extends MpAutoReplyBaseVO {

    @Schema(description = "主键", required = true, example = "1024")
    private Long id;

    @Schema(description = "公众号账号的编号", required = true, example = "1024")
    private Long accountId;
    @Schema(description = "公众号 appId", required = true, example = "wx1234567890")
    private String appId;

    @Schema(description = "创建时间", required = true)
    private Date createTime;

}
