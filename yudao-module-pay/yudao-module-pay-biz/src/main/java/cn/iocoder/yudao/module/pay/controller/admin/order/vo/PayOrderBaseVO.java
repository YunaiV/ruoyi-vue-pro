package cn.iocoder.yudao.module.pay.controller.admin.order.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 支付订单 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author aquan
 */
@Data
public class PayOrderBaseVO {

    @ApiModelProperty(value = "商户编号", required = true)
    @NotNull(message = "商户编号不能为空")
    private Long merchantId;

    @ApiModelProperty(value = "应用编号", required = true)
    @NotNull(message = "应用编号不能为空")
    private Long appId;

    @ApiModelProperty(value = "渠道编号")
    private Long channelId;

    @ApiModelProperty(value = "渠道编码")
    private String channelCode;

    @ApiModelProperty(value = "商户订单编号", required = true)
    @NotNull(message = "商户订单编号不能为空")
    private String merchantOrderId;

    @ApiModelProperty(value = "商品标题", required = true)
    @NotNull(message = "商品标题不能为空")
    private String subject;

    @ApiModelProperty(value = "商品描述", required = true)
    @NotNull(message = "商品描述不能为空")
    private String body;

    @ApiModelProperty(value = "异步通知地址", required = true)
    @NotNull(message = "异步通知地址不能为空")
    private String notifyUrl;

    @ApiModelProperty(value = "通知商户支付结果的回调状态", required = true)
    @NotNull(message = "通知商户支付结果的回调状态不能为空")
    private Integer notifyStatus;

    @ApiModelProperty(value = "支付金额，单位：分", required = true)
    @NotNull(message = "支付金额，单位：分不能为空")
    private Long amount;

    @ApiModelProperty(value = "渠道手续费，单位：百分比")
    private Double channelFeeRate;

    @ApiModelProperty(value = "渠道手续金额，单位：分")
    private Long channelFeeAmount;

    @ApiModelProperty(value = "支付状态", required = true)
    @NotNull(message = "支付状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "用户 IP", required = true)
    @NotNull(message = "用户 IP不能为空")
    private String userIp;

    @ApiModelProperty(value = "订单失效时间", required = true)
    @NotNull(message = "订单失效时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date expireTime;

    @ApiModelProperty(value = "订单支付成功时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date successTime;

    @ApiModelProperty(value = "订单支付通知时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date notifyTime;

    @ApiModelProperty(value = "支付成功的订单拓展单编号")
    private Long successExtensionId;

    @ApiModelProperty(value = "退款状态", required = true)
    @NotNull(message = "退款状态不能为空")
    private Integer refundStatus;

    @ApiModelProperty(value = "退款次数", required = true)
    @NotNull(message = "退款次数不能为空")
    private Integer refundTimes;

    @ApiModelProperty(value = "退款总金额，单位：分", required = true)
    @NotNull(message = "退款总金额，单位：分不能为空")
    private Long refundAmount;

    @ApiModelProperty(value = "渠道用户编号")
    private String channelUserId;

    @ApiModelProperty(value = "渠道订单号")
    private String channelOrderNo;

}
