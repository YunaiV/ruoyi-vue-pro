package cn.iocoder.yudao.module.pay.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 支付订单详细信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderDetailsRespVO extends PayOrderBaseVO {

    @Schema(title = "支付订单编号")
    private Long id;

    @Schema(title = "商户名称")
    private String merchantName;

    @Schema(title = "应用名称")
    private String appName;

    @Schema(title = "渠道编号名称")
    private String channelCodeName;

    @Schema(title = "创建时间")
    private LocalDateTime createTime;

    /**
     * 支付订单扩展
     */
    private PayOrderExtension payOrderExtension;

    @Data
    @Schema(title = "支付订单扩展")
    public static class PayOrderExtension {

        @Schema(title = "支付订单号")
        private String no;

        @Schema(title = "支付异步通知的内容")
        private String channelNotifyData;
    }

}
