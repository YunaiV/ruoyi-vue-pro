package cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 短信渠道 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsChannelRespVO extends SmsChannelBaseVO {

    @Schema(description = "编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "渠道编码,参见 SmsChannelEnum 枚举类", required = true, example = "YUN_PIAN")
    private String code;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
