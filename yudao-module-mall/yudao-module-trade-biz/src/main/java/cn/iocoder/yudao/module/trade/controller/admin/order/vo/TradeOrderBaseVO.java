package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
* 交易订单 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class TradeOrderBaseVO {

    // ========== 订单基本信息 ==========

    @ApiModelProperty(value = "订单编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "订单流水号", required = true, example = "1146347329394184195")
    private String no;

    @ApiModelProperty(value = "创建时间", required = true, notes = "下单时间")
    private Date createTime;

    @ApiModelProperty(value = "订单来源", required = true, example = "1", notes = "参见 TerminalEnum 枚举")
    private Integer terminal;

    @ApiModelProperty(value = "用户编号", required = true, example = "2048")
    private Long userId;

    @ApiModelProperty(value = "用户 IP", required = true, example = "127.0.0.1")
    private String userIp;

    @ApiModelProperty(value = "用户备注", required = true, example = "你猜")
    private String userRemark;

    @ApiModelProperty(value = "订单状态", required = true, example = "1", notes = "参见 TradeOrderStatusEnum 枚举")
    private Integer status;

    @ApiModelProperty(value = "购买的商品数量", required = true, example = "10")
    private Integer productCount;

    @ApiModelProperty(value = "订单完成时间")
    private LocalDateTime finishTime;

    @ApiModelProperty(value = "订单取消时间")
    private LocalDateTime cancelTime;

    @ApiModelProperty(value = "取消类型", example = "10", notes = "参见 TradeOrderCancelTypeEnum 枚举")
    private Integer cancelType;

    @ApiModelProperty(value = "商家备注", example = "你猜一下")
    private String remark;

    // ========== 价格 + 支付基本信息 ==========

    @ApiModelProperty(value = "支付订单编号", required = true, example = "1024")
    private Long payOrderId;

    @ApiModelProperty(value = "是否已支付", required = true, example = "true")
    private Boolean payed;

    @ApiModelProperty(value = "付款时间")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "支付渠道", required = true, example = "wx_lite", notes = "参见 PayChannelEnum 枚举")
    private String payChannelCode;

    @ApiModelProperty(value = "商品原价（总）", required = true, example = "1000", notes = "单位：分")
    private Integer originalPrice;

    @ApiModelProperty(value = "订单原价（总）", required = true, example = "1000", notes = "单位：分")
    private Integer orderPrice;

    @ApiModelProperty(value = "订单优惠（总）", required = true, example = "100", notes = "单位：分")
    private Integer discountPrice;

    @ApiModelProperty(value = "运费金额", required = true, example = "100", notes = "单位：分")
    private Integer deliveryPrice;

    @ApiModelProperty(value = "订单调价（总）", required = true, example = "100", notes = "单位：分")
    private Integer adjustPrice;

    @ApiModelProperty(value = "应付金额（总）", required = true, example = "1000", notes = "单位：分")
    private Integer payPrice;

    // ========== 收件 + 物流基本信息 ==========

    @ApiModelProperty(value = "配送模板编号", example = "1024")
    private Long deliveryTemplateId;

    @ApiModelProperty(value = "发货物流公司编号", example = "1024")
    private Long logisticsId;

    @ApiModelProperty(value = "发货物流单号", example = "1024")
    private String logisticsNo;

    @ApiModelProperty(value = "发货状态", required = true, example = "1", notes = "参见 TradeOrderDeliveryStatusEnum 枚举")
    private Integer deliveryStatus;

    @ApiModelProperty(value = "发货时间")
    private LocalDateTime deliveryTime;

    @ApiModelProperty(value = "收货时间")
    private LocalDateTime receiveTime;

    @ApiModelProperty(value = "收件人名称", required = true, example = "张三")
    private String receiverName;

    @ApiModelProperty(value = "收件人手机", required = true, example = "13800138000")
    private String receiverMobile;

    @ApiModelProperty(value = "收件人地区编号", required = true, example = "110000")
    private Integer receiverAreaId;

    @ApiModelProperty(value = "收件人邮编", required = true, example = "100000")
    private Integer receiverPostCode;

    @ApiModelProperty(value = "收件人详细地址", required = true, example = "中关村大街 1 号")
    private String receiverDetailAddress;

    // ========== 售后基本信息 ==========

    @ApiModelProperty(value = "售后状态", example = "1", notes = "参见 TradeOrderAfterSaleStatusEnum 枚举")
    private Integer afterSaleStatus;

    @ApiModelProperty(value = "退款金额", required = true, example = "100", notes = "单位：分")
    private Integer refundPrice;

    // ========== 营销基本信息 ==========

    @ApiModelProperty(value = "优惠劵编号", example = "1024")
    private Long couponId;

    @ApiModelProperty(value = "优惠劵减免金额", required = true, example = "100", notes = "单位：分")
    private Integer couponPrice;

    @ApiModelProperty(value = "积分抵扣的金额", required = true, example = "100", notes = "单位：分")
    private Integer pointPrice;
}
