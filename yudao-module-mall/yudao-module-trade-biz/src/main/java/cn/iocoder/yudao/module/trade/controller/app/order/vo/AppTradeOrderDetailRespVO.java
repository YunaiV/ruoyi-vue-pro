package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Schema(description = "用户 App - 订单交易的明细 Response VO")
@Data
public class AppTradeOrderDetailRespVO {

    // ========== 订单基本信息 ==========

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "订单流水号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1146347329394184195")
    private String no;

    @Schema(description = "下单时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date createTime;

    @Schema(description = "用户备注", requiredMode = Schema.RequiredMode.REQUIRED, example = "你猜")
    private String userRemark;

    @Schema(description = "订单状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "购买的商品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer productCount;

    @Schema(description = "订单完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "订单取消时间")
    private LocalDateTime cancelTime;

    // ========== 价格 + 支付基本信息 ==========

    @Schema(description = "支付订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long payOrderId;

    @Schema(description = "付款时间")
    private LocalDateTime payTime;

    @Schema(description = "商品原价（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer originalPrice;

    @Schema(description = "订单原价（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer orderPrice;

    @Schema(description = "订单优惠（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer discountPrice;

    @Schema(description = "运费金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer deliveryPrice;

    @Schema(description = "订单调价（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer adjustPrice;

    @Schema(description = "应付金额（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer payPrice;

    // ========== 收件 + 物流基本信息 ==========

    @Schema(description = "发货物流单号", example = "1024")
    private String logisticsNo;

    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "收件人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String receiverName;

    @Schema(description = "收件人手机", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String receiverMobile;

    @Schema(description = "收件人地区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "110000")
    private Integer receiverAreaId;

    @Schema(description = "收件人地区名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海 上海市 普陀区")
    private String receiverAreaName;

    @Schema(description = "收件人邮编", requiredMode = Schema.RequiredMode.REQUIRED, example = "100000")
    private Integer receiverPostCode;

    @Schema(description = "收件人详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "中关村大街 1 号")
    private String receiverDetailAddress;

    // ========== 售后基本信息 ==========

    // ========== 营销基本信息 ==========

    @Schema(description = "优惠劵编号", example = "1024")
    private Long couponId;

    @Schema(description = "优惠劵减免金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer couponPrice;

    @Schema(description = "积分抵扣的金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer pointPrice;

    /**
     * 订单项数组
     */
    private List<Item> items;

    @Schema(description = "用户 App - 交易订单的分页项的订单项目")
    @Data
    public static class Item {

        @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long spuId;

        @Schema(description = "商品 SPU 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
        private String spuName;

        @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long skuId;

        @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
        private String picUrl;

        @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer count;

        @Schema(description = "商品原价（总）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer originalPrice;

        @Schema(description = "商品原价（单）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer originalUnitPrice;

        /**
         * 属性数组
         */
        private List<AppProductPropertyValueDetailRespVO> properties;

    }

}
