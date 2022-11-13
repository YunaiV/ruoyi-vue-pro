package cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.coupon;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
* 优惠劵 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class CouponBaseVO {

    // ========== 基本信息 BEGIN ==========
    @ApiModelProperty(value = "优惠劵模板编号", required = true, example = "1024")
    @NotNull(message = "优惠劵模板编号不能为空")
    private Integer templateId;

    @ApiModelProperty(value = "优惠劵名", required = true, example = "春节送送送")
    @NotNull(message = "优惠劵名不能为空")
    private String name;

    @ApiModelProperty(value = "优惠码状态", required = true, example = "1", notes = "参见 CouponStatusEnum 枚举")
    private Integer status;

    // ========== 基本信息 END ==========

    // ========== 领取情况 BEGIN ==========
    @ApiModelProperty(value = "用户编号", required = true, example = "1")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @ApiModelProperty(value = "领取方式", required = true, example = "1", notes = "参见 CouponTakeTypeEnum 枚举类")
    @NotNull(message = "领取方式不能为空")
    private Integer takeType;
    // ========== 领取情况 END ==========

    // ========== 使用规则 BEGIN ==========
    @ApiModelProperty(value = "是否设置满多少金额可用", required = true, example = "100", notes = "单位：分；0 - 不限制")
    @NotNull(message = "是否设置满多少金额可用不能为空")
    private Integer usePrice;

    @ApiModelProperty(value = "固定日期 - 生效开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime validStartTime;

    @ApiModelProperty(value = "固定日期 - 生效结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime validEndTime;

    @ApiModelProperty(value = "商品范围", required = true, example = "1", notes = "参见 PromotionProductScopeEnum 枚举类")
    @NotNull(message = "商品范围不能为空")
    @InEnum(PromotionProductScopeEnum.class)
    private Integer productScope;

    @ApiModelProperty(value = "商品 SPU 编号的数组", example = "1,3")
    private List<Long> productSpuIds;
    // ========== 使用规则 END ==========

    // ========== 使用效果 BEGIN ==========
    @ApiModelProperty(value = "优惠类型", required = true, example = "1", notes = "参见 PromotionDiscountTypeEnum 枚举")
    @NotNull(message = "优惠类型不能为空")
    @InEnum(PromotionDiscountTypeEnum.class)
    private Integer discountType;

    @ApiModelProperty(value = "折扣百分比", example = "80", notes = "例如说，80% 为 80")
    private Integer discountPercent;

    @ApiModelProperty(value = "优惠金额", example = "10", notes = "单位：分")
    @Min(value = 0, message = "优惠金额需要大于等于 0")
    private Integer discountPrice;

    @ApiModelProperty(value = "折扣上限", example = "100", notes = "单位：分，仅在 discountType 为 PERCENT 使用")
    private Integer discountLimitPrice;
    // ========== 使用效果 END ==========

    // ========== 使用情况 BEGIN ==========

    @ApiModelProperty(value = "使用订单号", example = "4096")
    private Long useOrderId;

    @ApiModelProperty(value = "使用时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime useTime;

    // ========== 使用情况 END ==========

}
