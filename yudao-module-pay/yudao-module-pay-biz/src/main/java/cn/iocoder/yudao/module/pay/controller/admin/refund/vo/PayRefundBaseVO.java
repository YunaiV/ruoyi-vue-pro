package cn.iocoder.yudao.module.pay.controller.admin.refund.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
* 退款订单 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayRefundBaseVO {

    @Schema(description = "外部退款号", requiredMode = Schema.RequiredMode.REQUIRED, example = "110")
    private String no;

    @Schema(description = "应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long appId;

    @Schema(description = "渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long channelId;

    @Schema(description = "渠道编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "wx_app")
    private String channelCode;

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long orderId;

    // ========== 商户相关字段 ==========

    @Schema(description = "商户订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "225")
    private String merchantOrderId;

    @Schema(description = "商户退款订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "512")
    private String merchantRefundId;

    @Schema(description = "异步通知地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String notifyUrl;

    // ========== 退款相关字段 ==========

    @Schema(description = "退款状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long payPrice;

    @Schema(description = "退款金额,单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Long refundPrice;

    @Schema(description = "退款原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "我要退了")
    private String reason;

    @Schema(description = "用户 IP", requiredMode = Schema.RequiredMode.REQUIRED, example = "127.0.0.1")
    private String userIp;

    // ========== 渠道相关字段 ==========

    @Schema(description = "渠道订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "233")
    private String channelOrderNo;

    @Schema(description = "渠道退款单号", example = "2022")
    private String channelRefundNo;

    @Schema(description = "退款成功时间")
    private LocalDateTime successTime;

    @Schema(description = "调用渠道的错误码")
    private String channelErrorCode;

    @Schema(description = "调用渠道的错误提示")
    private String channelErrorMsg;

    @Schema(description = "支付渠道的额外参数")
    private String channelNotifyData;

}
