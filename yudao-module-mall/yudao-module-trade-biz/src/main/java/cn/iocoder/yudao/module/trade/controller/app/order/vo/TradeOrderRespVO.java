package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import lombok.*;
import io.swagger.annotations.*;

import java.time.LocalDateTime;
import java.util.*;

@ApiModel("订单交易 Response VO")
@Data
public class TradeOrderRespVO {

    @ApiModelProperty(value = "订单编号", required = true)
    private Integer id;
    @ApiModelProperty(value = "用户编号", required = true)
    private Integer userId;
    @ApiModelProperty(value = "订单单号", required = true)
    private String orderNo;
    @ApiModelProperty(value = "订单状态", required = true)
    private Integer orderStatus;
    @ApiModelProperty(value = "备注")
    private String remark;
    @ApiModelProperty(value = "订单结束时间")
    private LocalDateTime endTime;
    @ApiModelProperty(value = "订单金额(总金额)，单位：分", required = true)
    private Integer buyPrice;
    @ApiModelProperty(value = "优惠总金额，单位：分", required = true)
    private Integer discountPrice;
    @ApiModelProperty(value = "物流金额，单位：分", required = true)
    private Integer logisticsPrice;
    @ApiModelProperty(value = "最终金额，单位：分", required = true)
    private Integer presentPrice;
    @ApiModelProperty(value = "支付金额，单位：分", required = true)
    private Integer payPrice;
    @ApiModelProperty(value = "退款金额，单位：分", required = true)
    private Integer refundPrice;
    @ApiModelProperty(value = "付款时间")
    private LocalDateTime payTime;
    @ApiModelProperty(value = "支付订单编号")
    private Integer payTransactionId;
    @ApiModelProperty(value = "支付渠道")
    private Integer payChannel;
    @ApiModelProperty(value = "配送类型", required = true)
    private Integer deliveryType;
    @ApiModelProperty(value = "发货时间")
    private LocalDateTime deliveryTime;
    @ApiModelProperty(value = "收货时间")
    private LocalDateTime receiveTime;
    @ApiModelProperty(value = "收件人名称", required = true)
    private String receiverName;
    @ApiModelProperty(value = "手机号", required = true)
    private String receiverMobile;
    @ApiModelProperty(value = "地区编码", required = true)
    private Integer receiverAreaCode;
    @ApiModelProperty(value = "收件详细地址", required = true)
    private String receiverDetailAddress;
    @ApiModelProperty(value = "售后状态", required = true)
    private Integer afterSaleStatus;
    @ApiModelProperty(value = "优惠劵编号")
    private Integer couponCardId;
    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    /**
     * 订单项数组
     *
     * // TODO 芋艿，后续考虑怎么优化下，目前是内嵌了别的 dto
     */
    private List<TradeOrderItemRespVO> orderItems;


}
