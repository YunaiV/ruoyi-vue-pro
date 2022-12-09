package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 支付渠道 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelRespVO extends PayChannelBaseVO {

    @Schema(title = "商户编号", required = true)
    private Long id;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(title = "配置", required = true)
    private String config;
}
