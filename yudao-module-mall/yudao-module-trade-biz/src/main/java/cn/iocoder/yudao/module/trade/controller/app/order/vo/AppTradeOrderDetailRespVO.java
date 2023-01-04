package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@ApiModel("用户 App - 订单交易的明细 Response VO")
@Data
public class AppTradeOrderDetailRespVO {

    // ========== 订单基本信息 ==========

    @ApiModelProperty(value = "订单编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "订单流水号", required = true, example = "1146347329394184195")
    private String no;

    @ApiModelProperty(value = "创建时间", required = true, notes = "下单时间")
    private Date createTime;

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

    // ========== 价格 + 支付基本信息 ==========

    @ApiModelProperty(value = "支付订单编号", required = true, example = "1024")
    private Long payOrderId;

    @ApiModelProperty(value = "付款时间")
    private LocalDateTime payTime;

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

    @ApiModelProperty(value = "发货物流单号", example = "1024")
    private String logisticsNo;

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

    @ApiModelProperty(value = "收件人地区名字", required = true, example = "上海 上海市 普陀区")
    private String receiverAreaName;

    @ApiModelProperty(value = "收件人邮编", required = true, example = "100000")
    private Integer receiverPostCode;

    @ApiModelProperty(value = "收件人详细地址", required = true, example = "中关村大街 1 号")
    private String receiverDetailAddress;

    // ========== 售后基本信息 ==========

    // ========== 营销基本信息 ==========

    @ApiModelProperty(value = "优惠劵编号", example = "1024")
    private Long couponId;

    @ApiModelProperty(value = "优惠劵减免金额", required = true, example = "100", notes = "单位：分")
    private Integer couponPrice;

    @ApiModelProperty(value = "积分抵扣的金额", required = true, example = "100", notes = "单位：分")
    private Integer pointPrice;

    /**
     * 订单项数组
     */
    private List<Item> items;

    @ApiModel("用户 App - 交易订单的分页项的订单项目")
    @Data
    public static class Item {

        @ApiModelProperty(value = "编号", required = true, example = "1")
        private Long id;

        @ApiModelProperty(value = "商品 SPU 编号", required = true, example = "1")
        private Long spuId;

        @ApiModelProperty(value = "商品 SPU 名称", required = true, example = "芋道源码")
        private String spuName;

        @ApiModelProperty(value = "商品 SKU 编号", required = true, example = "1")
        private Long skuId;

        @ApiModelProperty(value = "商品图片", required = true, example = "https://www.iocoder.cn/1.png")
        private String picUrl;

        @ApiModelProperty(value = "购买数量", required = true, example = "1")
        private Integer count;

        @ApiModelProperty(value = "商品原价（总）", required = true, example = "100", notes = "单位：分")
        private Integer originalPrice;

        @ApiModelProperty(value = "商品原价（单）", required = true, example = "100", notes = "单位：分")
        private Integer originalUnitPrice;

        /**
         * 属性数组
         */
        private List<AppProductPropertyValueDetailRespVO> properties;

    }

}
