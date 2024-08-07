package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "用户 App - 交易订单结算信息 Response VO")
@Data
public class AppTradeOrderSettlementRespVO {

    @Schema(description = "交易类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1") // 对应 TradeOrderTypeEnum 枚举
    private Integer type;

    @Schema(description = "购物项数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "费用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Price price;

    @Schema(description = "收件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private Address address;

    @Schema(description = "已使用的积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer usePoint;

    @Schema(description = "总积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer totalPoint;

    @Schema(description = "购物项")
    @Data
    public static class Item {

        // ========== SPU 信息 ==========

        @Schema(description = "品类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
        private Long categoryId;
        @Schema(description = "SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
        private Long spuId;
        @Schema(description = "SPU 名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "Apple iPhone 12")
        private String spuName;

        // ========== SKU 信息 ==========

        @Schema(description = "SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Integer skuId;
        @Schema(description = "价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer price;
        @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
        private String picUrl;

        @Schema(description = "属性数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private List<AppProductPropertyValueDetailRespVO> properties;

        // ========== 购物车信息 ==========

        @Schema(description = "购物车编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Long cartId;

        @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer count;

    }

    @Schema(description = "费用（合计）")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Price {

        @Schema(description = "商品原价（总），单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
        private Integer totalPrice;

        @Schema(description = "订单优惠（总），单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "66")
        private Integer discountPrice;

        @Schema(description = "运费金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        private Integer deliveryPrice;

        @Schema(description = "优惠劵减免金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer couponPrice;

        @Schema(description = "积分抵扣的金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        private Integer pointPrice;

        @Schema(description = "VIP 减免金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
        private Integer vipPrice;

        @Schema(description = "实际支付金额（总），单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "450")
        private Integer payPrice;

    }

    @Schema(description = "地址信息")
    @Data
    public static class Address {

        @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "收件人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小王")
        private String name;

        @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
        private String mobile;

        @Schema(description = "地区编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "地区编号不能为空")
        private Long areaId;
        @Schema(description = "地区名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海上海市普陀区")
        private String areaName;

        @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "望京悠乐汇 A 座")
        private String detailAddress;

        @Schema(description = "是否默认收件地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        private Boolean defaultStatus;

    }

}
