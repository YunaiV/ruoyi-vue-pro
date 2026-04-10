package cn.iocoder.yudao.module.pay.controller.admin.channel.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 支付渠道 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelRespVO extends PayChannelBaseVO {

    @Schema(description = "商户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private LocalDateTime createTime;

    @Schema(description = "渠道编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "alipay_pc")
    private String code;

    @Schema(description = "配置", requiredMode = Schema.RequiredMode.REQUIRED)
    private String config;

}
