package cn.iocoder.yudao.module.pay.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 支付订单 Excel 导出 Request VO，参数和 PayOrderPageReqVO 是一致的")
@Data
public class PayOrderExportReqVO {

    @Schema(description = "商户编号")
    private Long merchantId;

    @Schema(description = "应用编号")
    private Long appId;

    @Schema(description = "渠道编号")
    private Long channelId;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "商户订单编号")
    private String merchantOrderId;

    @Schema(description = "商品标题")
    private String subject;

    @Schema(description = "商品描述")
    private String body;

    @Schema(description = "异步通知地址")
    private String notifyUrl;

    @Schema(description = "通知商户支付结果的回调状态")
    private Integer notifyStatus;

    @Schema(description = "支付金额，单位：分")
    private Long amount;

    @Schema(description = "渠道手续费，单位：百分比")
    private Double channelFeeRate;

    @Schema(description = "渠道手续金额，单位：分")
    private Long channelFeeAmount;

    @Schema(description = "支付状态")
    private Integer status;

    @Schema(description = "用户 IP")
    private String userIp;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "订单失效时间")
    private LocalDateTime[] expireTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "开始订单支付成功时间")
    private LocalDateTime[] successTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "开始订单支付通知时间")
    private LocalDateTime[] notifyTime;

    @Schema(description = "支付成功的订单拓展单编号")
    private Long successExtensionId;

    @Schema(description = "退款状态")
    private Integer refundStatus;

    @Schema(description = "退款次数")
    private Integer refundTimes;

    @Schema(description = "退款总金额，单位：分")
    private Long refundAmount;

    @Schema(description = "渠道用户编号")
    private String channelUserId;

    @Schema(description = "渠道订单号")
    private String channelOrderNo;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
