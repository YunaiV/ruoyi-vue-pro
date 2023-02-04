package cn.iocoder.yudao.module.pay.controller.admin.refund.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 退款订单详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayRefundDetailsRespVO extends PayRefundBaseVO {

    @Schema(description = "支付退款编号", required = true)
    private Long id;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "渠道编号名称")
    private String channelCodeName;

    @NotNull(message = "商品标题不能为空")
    private String subject;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
