package cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template;

import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 优惠劵模板 Response VO")
@Data
public class AppCouponTemplateRespVO {

    @Schema(description = "优惠劵模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "优惠劵名", requiredMode = Schema.RequiredMode.REQUIRED, example = "春节送送送")
    private String name;

    @Schema(description = "每人限领个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "66") // -1 - 则表示不限制
    private Integer takeLimitCount;

    @Schema(description = "是否设置满多少金额可用", requiredMode = Schema.RequiredMode.REQUIRED, example = "100") // 单位：分；0 - 不限制
    private Integer usePrice;

    @Schema(description = "商品范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer productScope;

    @Schema(description = "商品范围编号的数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Long> productScopeValues;

    @Schema(description = "生效日期类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer validityType;

    @Schema(description = "固定日期 - 生效开始时间")
    private LocalDateTime validStartTime;

    @Schema(description = "固定日期 - 生效结束时间")
    private LocalDateTime validEndTime;

    @Schema(description = "领取日期 - 开始天数")
    @Min(value = 0L, message = "开始天数必须大于 0")
    private Integer fixedStartTerm;

    @Schema(description = "领取日期 - 结束天数")
    @Min(value = 1L, message = "开始天数必须大于 1")
    private Integer fixedEndTerm;

    @Schema(description = "优惠类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer discountType;

    @Schema(description = "折扣百分比", example = "80") //  例如说，80% 为 80
    private Integer discountPercent;

    @Schema(description = "优惠金额", example = "10")
    @Min(value = 0, message = "优惠金额需要大于等于 0")
    private Integer discountPrice;

    @Schema(description = "折扣上限", example = "100") // 单位：分，仅在 discountType 为 PERCENT 使用
    private Integer discountLimitPrice;

    // ========== 用户相关字段 ==========

    @Schema(description = "是否可以领取", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean canTake;

    @Schema(description = "优惠券说明", example = "优惠券使用说明") // 单位：分，仅在 discountType 为 PERCENT 使用
    private String description;

}
