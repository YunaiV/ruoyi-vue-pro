package cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 交易售后 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class AfterSaleBaseVO {

    @Schema(description = "售后流水号", requiredMode = Schema.RequiredMode.REQUIRED, example = "202211190847450020500077")
    @NotNull(message = "售后流水号不能为空")
    private String no;

    @Schema(description = "售后状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "售后状态不能为空")
    private Integer status;

    @Schema(description = "售后类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "售后类型不能为空")
    private Integer type;

    @Schema(description = "售后方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "售后方式不能为空")
    private Integer way;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30337")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "申请原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不喜欢")
    @NotNull(message = "申请原因不能为空")
    private String applyReason;

    @Schema(description = "补充描述", example = "你说的对")
    private String applyDescription;

    @Schema(description = "补充凭证图片", example = "https://www.iocoder.cn/1.png")
    private List<String> applyPicUrls;

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18078")
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    @Schema(description = "订单流水号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022111917190001")
    @NotNull(message = "订单流水号不能为空")
    private String orderNo;

    @Schema(description = "订单项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "572")
    @NotNull(message = "订单项编号不能为空")
    private Long orderItemId;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2888")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

    @Schema(description = "商品 SPU 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotNull(message = "商品 SPU 名称不能为空")
    private String spuName;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15657")
    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @Schema(description = "商品图片", example = "https://www.iocoder.cn/2.png")
    private String picUrl;

    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20012")
    @NotNull(message = "购买数量不能为空")
    private Integer count;

    @Schema(description = "审批时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime auditTime;

    @Schema(description = "审批人", example = "30835")
    private Long auditUserId;

    @Schema(description = "审批备注", example = "不香")
    private String auditReason;

    @Schema(description = "退款金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "18077")
    @NotNull(message = "退款金额，单位：分不能为空")
    private Integer refundPrice;

    @Schema(description = "支付退款编号", example = "10271")
    private Long payRefundId;

    @Schema(description = "退款时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime refundTime;

    @Schema(description = "退货物流公司编号", example = "10")
    private Long logisticsId;

    @Schema(description = "退货物流单号", example = "610003952009")
    private String logisticsNo;

    @Schema(description = "退货时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime deliveryTime;

    @Schema(description = "收货时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime receiveTime;

    @Schema(description = "收货备注", example = "不喜欢")
    private String receiveReason;

}
