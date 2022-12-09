package cn.iocoder.yudao.module.pay.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - 支付订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderPageReqVO extends PageParam {

    @Schema(title = "商户编号")
    private Long merchantId;

    @Schema(title = "应用编号")
    private Long appId;

    @Schema(title = "渠道编号")
    private Long channelId;

    @Schema(title = "渠道编码")
    private String channelCode;

    @Schema(title = "商户订单编号")
    private String merchantOrderId;

    @Schema(title = "商品标题")
    private String subject;

    @Schema(title = "商品描述")
    private String body;

    @Schema(title = "异步通知地址")
    private String notifyUrl;

    @Schema(title = "通知商户支付结果的回调状态")
    private Integer notifyStatus;

    @Schema(title = "支付金额，单位：分")
    private Long amount;

    @Schema(title = "渠道手续费，单位：百分比")
    private Double channelFeeRate;

    @Schema(title = "渠道手续金额，单位：分")
    private Long channelFeeAmount;

    @Schema(title = "支付状态")
    private Integer status;

    @Schema(title = "用户 IP")
    private String userIp;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "订单失效时间")
    private LocalDateTime[] expireTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "订单支付成功时间")
    private LocalDateTime[] successTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "订单支付通知时间")
    private LocalDateTime[] notifyTime;

    @Schema(title = "支付成功的订单拓展单编号")
    private Long successExtensionId;

    @Schema(title = "退款状态")
    private Integer refundStatus;

    @Schema(title = "退款次数")
    private Integer refundTimes;

    @Schema(title = "退款总金额，单位：分")
    private Long refundAmount;

    @Schema(title = "渠道用户编号")
    private String channelUserId;

    @Schema(title = "渠道订单号")
    private String channelOrderNo;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "创建时间")
    private LocalDateTime[] createTime;

}
