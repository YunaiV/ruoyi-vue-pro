package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "用户 App - 交易订单结算 Request VO")
@Data
public class AppTradeOrderSettlementReqVO {

    @NotNull(message = "交易类型不能为空")
    @InEnum(value = TradeOrderTypeEnum.class, message = "交易类型必须是 {value}")
    @Deprecated // TODO 芋艿：后续干掉这个字段，对于前端不需要关注这个
    private Integer type;

    @Schema(description = "商品项数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品不能为空")
    private List<Item> items;

    @Schema(description = "收件地址编号", example = "1")
    private Long addressId;

    @Schema(description = "优惠劵编号", example = "1024")
    private Long couponId;

    // ========== 秒杀活动相关字段 ==========
    @Schema(description = "秒杀活动编号", example = "1024")
    private Long seckillActivityId;

    // ========== 拼团活动相关字段 ==========
    @Schema(description = "拼团活动编号", example = "1024")
    private Long combinationActivityId;

    @Schema(description = "拼团团长编号", example = "2048")
    private Long combinationHeadId;

    @Data
    @Schema(description = "用户 App - 商品项")
    @Valid
    public static class Item {

        @Schema(description = "商品 SKU 编号", example = "2048")
        private Long skuId;
        @Schema(description = "购买数量", example = "1")
        @Min(value = 1, message = "购买数量最小值为 {value}")
        private Integer count;

        @Schema(description = "购物车项的编号", example = "1024")
        private Long cartId;

        @AssertTrue(message = "商品不正确")
        @JsonIgnore
        public boolean isValid() {
            // 组合一：skuId + count 使用商品 SKU
            if (skuId != null && count != null) {
                return true;
            }
            // 组合二：cartId 使用购物车项
            return cartId != null;
        }

    }

}
