package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "用户 App - 交易订单结算 Request VO")
@Data
@Valid
public class AppTradeOrderSettlementReqVO {

    @Schema(description = "商品项数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品不能为空")
    private List<Item> items;

    @Schema(description = "优惠劵编号", example = "1024")
    private Long couponId;

    @Schema(description = "是否使用积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否使用积分不能为空")
    private Boolean pointStatus;

    // ========== 配送相关相关字段 ==========
    @Schema(description = "配送方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @InEnum(value = DeliveryTypeEnum.class, message = "配送方式不正确")
    private Integer deliveryType;

    @Schema(description = "收件地址编号", example = "1")
    private Long addressId;

    @Schema(description = "自提门店编号", example = "1088")
    private Long pickUpStoreId;
    @Schema(description = "收件人名称", example = "芋艿") // 选择门店自提时，该字段为联系人名
    private String receiverName;
    @Schema(description = "收件人手机", example = "15601691300") // 选择门店自提时，该字段为联系人手机
    @Mobile(message = "收件人手机格式不正确")
    private String receiverMobile;

    // ========== 秒杀活动相关字段 ==========
    @Schema(description = "秒杀活动编号", example = "1024")
    private Long seckillActivityId;

    // ========== 拼团活动相关字段 ==========
    @Schema(description = "拼团活动编号", example = "1024")
    private Long combinationActivityId;

    @Schema(description = "拼团团长编号", example = "2048")
    private Long combinationHeadId;

    // ========== 砍价活动相关字段 ==========
    @Schema(description = "砍价记录编号", example = "123")
    private Long bargainRecordId;

    // ========== 积分商城活动相关字段 ==========
    @Schema(description = "积分商城活动编号", example = "123")
    private Long pointActivityId;

    @AssertTrue(message = "活动商品每次只能购买一种规格")
    @JsonIgnore
    public boolean isValidActivityItems() {
        // 校验是否是活动订单
        if (ObjUtil.isAllEmpty(seckillActivityId, combinationActivityId, combinationHeadId, bargainRecordId)) {
            return true;
        }
        // 校验订单项是否超出
        return items.size() == 1;
    }

    @Data
    @Schema(description = "用户 App - 商品项")
    @Valid
    public static class Item {

        @Schema(description = "商品 SKU 编号", example = "2048")
        @NotNull(message = "商品 SKU 编号不能为空")
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
