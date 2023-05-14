package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "用户 App - 交易订单结算信息 Response VO")
@Data
public class AppTradeOrderSettlementRespVO {

    @Schema(description = "购物项数组", required = true)
    private List<Item> items;

    @Schema(description = "费用", required = true)
    private Price price;

    @Schema(description = "收件地址", required = true)
    private Address address;

    @Schema(description = "购物项")
    @Data
    public static class Item {

        // ========== SPU 信息 ==========

        @Schema(description = "SPU 编号", required = true, example = "2048")
        private Long spuId;
        @Schema(description = "SPU 名字", required = true, example = "Apple iPhone 12")
        private String spuName;

        // ========== SKU 信息 ==========

        @Schema(description = "SKU 编号", required = true, example = "1024")
        private Integer skuId;
        @Schema(description = "价格，单位：分", required = true, example = "100")
        private Integer price;
        @Schema(description = "图片地址", required = true, example = "https://www.iocoder.cn/1.png")
        private String picUrl;

        @Schema(description = "属性数组", required = true, example = "100")
        private List<AppProductPropertyValueDetailRespVO> properties;

        // ========== 购物车信息 ==========

        @Schema(description = "购物车编号", required = true, example = "100")
        private Long cartId;

        @Schema(description = "购买数量", required = true, example = "1")
        private Integer count;

    }

    @Schema(description = "费用（合计）")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Price {

        @Schema(description = "商品原价（总），单位：分", required = true, example = "500")
        private Integer originalPrice;

        @Schema(description = "运费金额，单位：分", required = true, example = "50")
        private Integer deliveryPrice;

        @Schema(description = "优惠劵减免金额，单位：分", required = true, example = "100")
        private Integer couponPrice;

        @Schema(description = "积分抵扣的金额，单位：分", required = true, example = "50")
        private Integer pointPrice;

        @Schema(description = "实际支付金额（总），单位：分", required = true, example = "450")
        private Integer payPrice;

    }

    @Schema(description = "费用（合计）")
    @Data
    public static class Address {

        @Schema(description = "编号", required = true, example = "1")
        private Long id;

        @Schema(description = "收件人名称", required = true, example = "小王")
        private String name;

        @Schema(description = "手机号", required = true, example = "15601691300")
        private String mobile;

        @Schema(description = "省份编号", required = true, example = "1")
        private Long provinceId;
        @Schema(description = "省份名字", required = true, example = "北京")
        private String provinceName;

        @Schema(description = "城市编号", required = true, example = "1")
        private Long cityId;
        @Schema(description = "城市名字", required = true, example = "北京")
        private String cityName;

        @Schema(description = "地区编号", required = true, example = "1")
        private Long districtId;
        @Schema(description = "地区名字", required = true, example = "朝阳区")
        private String districtName;

        @Schema(description = "详细地址", required = true, example = "望京悠乐汇 A 座")
        private String detailAddress;

        @Schema(description = "是否默认收件地址", required = true, example = "true")
        private Boolean defaulted;

    }

}
