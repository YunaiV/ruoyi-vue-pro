package cn.iocoder.yudao.module.pay.controller.admin.refund.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 退款订单 Excel 导出 Request VO，参数和 PayRefundPageReqVO 是一致的")
@Data
public class PayRefundExportReqVO {

    @Schema(description = "商户编号")
    private Long merchantId;

    @Schema(description = "应用编号")
    private Long appId;

    @Schema(description = "渠道编号")
    private Long channelId;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "支付订单编号 pay_order 表id")
    private Long orderId;

    @Schema(description = "交易订单号 pay_extension 表no 字段")
    private String tradeNo;

    @Schema(description = "商户订单编号（商户系统生成）")
    private String merchantOrderId;

    @Schema(description = "商户退款订单号（商户系统生成）")
    private String merchantRefundNo;

    @Schema(description = "异步通知商户地址")
    private String notifyUrl;

    @Schema(description = "通知商户退款结果的回调状态")
    private Integer notifyStatus;

    @Schema(description = "退款状态")
    private Integer status;

    @Schema(description = "退款类型(部分退款，全部退款)")
    private Integer type;

    @Schema(description = "支付金额,单位分")
    private Long payAmount;

    @Schema(description = "退款金额,单位分")
    private Long refundAmount;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "用户 IP")
    private String userIp;

    @Schema(description = "渠道订单号，pay_order 中的channel_order_no 对应")
    private String channelOrderNo;

    @Schema(description = "渠道退款单号，渠道返回")
    private String channelRefundNo;

    @Schema(description = "渠道调用报错时，错误码")
    private String channelErrorCode;

    @Schema(description = "渠道调用报错时，错误信息")
    private String channelErrorMsg;

    @Schema(description = "支付渠道的额外参数")
    private String channelExtras;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "退款失效时间")
    private LocalDateTime[] expireTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "退款成功时间")
    private LocalDateTime[] successTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "退款通知时间")
    private LocalDateTime[] notifyTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
