package cn.iocoder.yudao.module.promotion.controller.admin.reward.vo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* 满减送活动 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class RewardActivityBaseVO {

    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "满啦满啦")
    @NotNull(message = "活动标题不能为空")
    private String name;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须大于当前时间")
    private LocalDateTime endTime;

    @Schema(description = "备注", example = "biubiubiu")
    private String remark;

    @Schema(description = "条件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "条件类型不能为空")
    @InEnum(value = PromotionConditionTypeEnum.class, message = "条件类型必须是 {value}")
    private Integer conditionType;

    @Schema(description = "商品范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品范围不能为空")
    @InEnum(value = PromotionProductScopeEnum.class, message = "商品范围必须是 {value}")
    private Integer productScope;

    @Schema(description = "商品范围编号的数组", example = "[1, 3]")
    private List<Long> productScopeValues;

    /**
     * 优惠规则的数组
     */
    @Valid // 校验下子对象
    private List<Rule> rules;

    @Schema(description = "优惠规则")
    @Data
    public static class Rule {

        @Schema(description = "优惠门槛", requiredMode = Schema.RequiredMode.REQUIRED, example = "100") // 1. 满 N 元，单位：分; 2. 满 N 件
        @Min(value = 1L, message = "优惠门槛必须大于等于 1")
        private Integer limit;

        @Schema(description = "优惠价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @Min(value = 1L, message = "优惠价格必须大于等于 1")
        private Integer discountPrice;

        @Schema(description = "是否包邮", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull(message = "规则是否包邮不能为空")
        private Boolean freeDelivery;

        @Schema(description = "是否赠送积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull(message = "规则是否赠送积分不能为空")
        private Boolean givePoint;

        @Schema(description = "赠送的积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer point;

        @Schema(description = "是否赠送优惠券", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull(message = "规则是否赠送优惠券不能为空")
        private Boolean giveCoupon;

        @Schema(description = "赠送的优惠劵编号的数组", example = "1,2,3")
        private Map<Long, Integer> giveCouponsMap;

        @AssertTrue(message = "赠送的积分不能小于 1")
        @JsonIgnore
        public boolean isPointValid() {
            return BooleanUtil.isFalse(givePoint) || (point != null && point >= 1);
        }

    }

    @AssertTrue(message = "商品范围编号的数组不能为空")
    @JsonIgnore
    public boolean isProductScopeValuesValid() {
        return Objects.equals(productScope, PromotionProductScopeEnum.ALL.getScope()) // 全部范围时，可以为空
                || CollUtil.isNotEmpty(productScopeValues);
    }

}
