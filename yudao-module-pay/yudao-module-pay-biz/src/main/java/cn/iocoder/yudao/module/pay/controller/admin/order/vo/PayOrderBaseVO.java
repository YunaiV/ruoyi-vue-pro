package cn.iocoder.yudao.module.pay.controller.admin.order.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 支付订单 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author aquan
 */
@Data
public class PayOrderBaseVO {

    @Schema(title = "商户编号", required = true)
    @NotNull(message = "商户编号不能为空")
    private Long merchantId;

    @Schema(title = "应用编号", required = true)
    @NotNull(message = "应用编号不能为空")
    private Long appId;

    @Schema(title = "渠道编号")
    private Long channelId;

    @Schema(title = "渠道编码")
    private String channelCode;

    @Schema(title = "商户订单编号", required = true)
    @NotNull(message = "商户订单编号不能为空")
    private String merchantOrderId;

    @Schema(title = "商品标题", required = true)
    @NotNull(message = "商品标题不能为空")
    private String subject;

    @Schema(title = "商品描述", required = true)
    @NotNull(message = "商品描述不能为空")
    private String body;

    @Schema(title = "异步通知地址", required = true)
    @NotNull(message = "异步通知地址不能为空")
    private String notifyUrl;

    @Schema(title = "通知商户支付结果的回调状态", required = true)
    @NotNull(message = "通知商户支付结果的回调状态不能为空")
    private Integer notifyStatus;

    @Schema(title = "支付金额，单位：分", required = true)
    @NotNull(message = "支付金额，单位：分不能为空")
    private Long amount;

    @Schema(title = "渠道手续费，单位：百分比")
    private Double channelFeeRate;

    @Schema(title = "渠道手续金额，单位：分")
    private Long channelFeeAmount;

    @Schema(title = "支付状态", required = true)
    @NotNull(message = "支付状态不能为空")
    private Integer status;

    @Schema(title = "用户 IP", required = true)
    @NotNull(message = "用户 IP不能为空")
    private String userIp;

    @Schema(title = "订单失效时间", required = true)
    @NotNull(message = "订单失效时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime expireTime;

    @Schema(title = "订单支付成功时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime successTime;

    @Schema(title = "订单支付通知时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime notifyTime;

    @Schema(title = "支付成功的订单拓展单编号")
    private Long successExtensionId;

    @Schema(title = "退款状态", required = true)
    @NotNull(message = "退款状态不能为空")
    private Integer refundStatus;

    @Schema(title = "退款次数", required = true)
    @NotNull(message = "退款次数不能为空")
    private Integer refundTimes;

    @Schema(title = "退款总金额，单位：分", required = true)
    @NotNull(message = "退款总金额，单位：分不能为空")
    private Long refundAmount;

    @Schema(title = "渠道用户编号")
    private String channelUserId;

    @Schema(title = "渠道订单号")
    private String channelOrderNo;

}
