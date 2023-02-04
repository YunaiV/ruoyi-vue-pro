package cn.iocoder.yudao.module.pay.controller.admin.refund.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 退款订单 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayRefundBaseVO {

    @Schema(description = "商户编号", required = true)
    @NotNull(message = "商户编号不能为空")
    private Long merchantId;

    @Schema(description = "应用编号", required = true)
    @NotNull(message = "应用编号不能为空")
    private Long appId;

    @Schema(description = "渠道编号", required = true)
    @NotNull(message = "渠道编号不能为空")
    private Long channelId;

    @Schema(description = "渠道编码", required = true)
    @NotNull(message = "渠道编码不能为空")
    private String channelCode;

    @Schema(description = "支付订单编号 pay_order 表id", required = true)
    @NotNull(message = "支付订单编号 pay_order 表id不能为空")
    private Long orderId;

    @Schema(description = "交易订单号 pay_extension 表no 字段", required = true)
    @NotNull(message = "交易订单号 pay_extension 表no 字段不能为空")
    private String tradeNo;

    @Schema(description = "商户订单编号（商户系统生成）", required = true)
    @NotNull(message = "商户订单编号（商户系统生成）不能为空")
    private String merchantOrderId;

    @Schema(description = "商户退款订单号（商户系统生成）", required = true)
    @NotNull(message = "商户退款订单号（商户系统生成）不能为空")
    private String merchantRefundNo;

    @Schema(description = "异步通知商户地址", required = true)
    @NotNull(message = "异步通知商户地址不能为空")
    private String notifyUrl;

    @Schema(description = "通知商户退款结果的回调状态", required = true)
    @NotNull(message = "通知商户退款结果的回调状态不能为空")
    private Integer notifyStatus;

    @Schema(description = "退款状态", required = true)
    @NotNull(message = "退款状态不能为空")
    private Integer status;

    @Schema(description = "退款类型(部分退款，全部退款)", required = true)
    @NotNull(message = "退款类型(部分退款，全部退款)不能为空")
    private Integer type;

    @Schema(description = "支付金额,单位分", required = true)
    @NotNull(message = "支付金额,单位分不能为空")
    private Long payAmount;

    @Schema(description = "退款金额,单位分", required = true)
    @NotNull(message = "退款金额,单位分不能为空")
    private Long refundAmount;

    @Schema(description = "退款原因", required = true)
    @NotNull(message = "退款原因不能为空")
    private String reason;

    @Schema(description = "用户 IP")
    private String userIp;

    @Schema(description = "渠道订单号，pay_order 中的channel_order_no 对应", required = true)
    @NotNull(message = "渠道订单号，pay_order 中的channel_order_no 对应不能为空")
    private String channelOrderNo;

    @Schema(description = "渠道退款单号，渠道返回")
    private String channelRefundNo;

    @Schema(description = "渠道调用报错时，错误码")
    private String channelErrorCode;

    @Schema(description = "渠道调用报错时，错误信息")
    private String channelErrorMsg;

    @Schema(description = "支付渠道的额外参数")
    private String channelExtras;

    @Schema(description = "退款失效时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime expireTime;

    @Schema(description = "退款成功时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime successTime;

    @Schema(description = "退款通知时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime notifyTime;

}
