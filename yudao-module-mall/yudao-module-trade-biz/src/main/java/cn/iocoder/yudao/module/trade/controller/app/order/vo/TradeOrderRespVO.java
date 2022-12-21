package cn.iocoder.yudao.module.trade.controller.app.order.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Schema(description = "订单交易 Response VO")
@Data
public class TradeOrderRespVO {

    @Schema(description = "订单编号", required = true)
    private Integer id;
    @Schema(description = "用户编号", required = true)
    private Integer userId;
    @Schema(description = "订单单号", required = true)
    private String orderNo;
    @Schema(description = "订单状态", required = true)
    private Integer orderStatus;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "订单结束时间")
    private LocalDateTime endTime;
    @Schema(description = "订单金额(总金额)，单位：分", required = true)
    private Integer buyPrice;
    @Schema(description = "优惠总金额，单位：分", required = true)
    private Integer discountPrice;
    @Schema(description = "物流金额，单位：分", required = true)
    private Integer logisticsPrice;
    @Schema(description = "最终金额，单位：分", required = true)
    private Integer presentPrice;
    @Schema(description = "支付金额，单位：分", required = true)
    private Integer payPrice;
    @Schema(description = "退款金额，单位：分", required = true)
    private Integer refundPrice;
    @Schema(description = "付款时间")
    private LocalDateTime payTime;
    @Schema(description = "支付订单编号")
    private Integer payTransactionId;
    @Schema(description = "支付渠道")
    private Integer payChannel;
    @Schema(description = "配送类型", required = true)
    private Integer deliveryType;
    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;
    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;
    @Schema(description = "收件人名称", required = true)
    private String receiverName;
    @Schema(description = "手机号", required = true)
    private String receiverMobile;
    @Schema(description = "地区编码", required = true)
    private Integer receiverAreaCode;
    @Schema(description = "收件详细地址", required = true)
    private String receiverDetailAddress;
    @Schema(description = "售后状态", required = true)
    private Integer afterSaleStatus;
    @Schema(description = "优惠劵编号")
    private Integer couponCardId;
    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    /**
     * 订单项数组
     *
     * // TODO 芋艿，后续考虑怎么优化下，目前是内嵌了别的 dto
     */
    private List<TradeOrderItemRespVO> orderItems;


}
