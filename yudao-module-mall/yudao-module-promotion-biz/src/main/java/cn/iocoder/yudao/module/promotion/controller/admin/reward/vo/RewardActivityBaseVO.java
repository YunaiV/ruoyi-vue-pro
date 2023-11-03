package cn.iocoder.yudao.module.promotion.controller.admin.reward.vo;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

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
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
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
    @InEnum(value = PromotionConditionTypeEnum.class, message = "商品范围必须是 {value}")
    private Integer productScope;

    @Schema(description = "商品 SPU 编号的数组", example = "1,2,3")
    private List<Long> productSpuIds;

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
        private Boolean freeDelivery;

        @Schema(description = "赠送的积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @Min(value = 1L, message = "赠送的积分必须大于等于 1")
        private Integer point;

        @Schema(description = "赠送的优惠劵编号的数组", example = "1,2,3")
        private List<Long> couponIds;

        @Schema(description = "赠送的优惠券数量的数组", example = "1,2,3")
        private List<Integer> couponCounts;

        @AssertTrue(message = "优惠劵和数量必须一一对应")
        @JsonIgnore
        public boolean isCouponCountsValid() {
            return CollUtil.size(couponCounts) == CollUtil.size(couponCounts);
        }

    }

}
