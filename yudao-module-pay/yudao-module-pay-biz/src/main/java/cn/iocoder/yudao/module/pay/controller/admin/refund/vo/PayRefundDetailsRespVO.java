package cn.iocoder.yudao.module.pay.controller.admin.refund.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 退款订单详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayRefundDetailsRespVO extends PayRefundBaseVO {

    @Schema(description = "支付退款编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "应用名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是芋艿")
    private String appName;

    @Schema(description = "支付订单", requiredMode = Schema.RequiredMode.REQUIRED)
    private Order order;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "管理后台 - 支付订单")
    @Data
    public static class Order {

        @Schema(description = "商品标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "土豆")
        private String subject;

    }

}
