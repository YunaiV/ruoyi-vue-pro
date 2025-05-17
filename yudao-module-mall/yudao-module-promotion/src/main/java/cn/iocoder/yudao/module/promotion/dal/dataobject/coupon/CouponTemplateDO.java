package cn.iocoder.yudao.module.promotion.dal.dataobject.coupon;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTakeTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTemplateValidityTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠劵模板 DO
 *
 * 当用户领取时，会生成 {@link CouponDO} 优惠劵
 *
 * @author 芋道源码
 */
@TableName(value = "promotion_coupon_template", autoResultMap = true)
@KeySequence("promotion_coupon_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class CouponTemplateDO extends BaseDO {

    /**
     * 不限制领取数量
     */
    public static final Integer TIME_LIMIT_COUNT_MAX = -1;

    // ========== 基本信息 BEGIN ==========
    /**
     * 模板编号，自增唯一
     */
    @TableId
    private Long id;
    /**
     * 优惠劵名
     */
    private String name;
    /**
     * 优惠券说明
     */
    private String description;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    // TODO 芋艿：要不要改成 3 个状态？？
    private Integer status;

    // ========== 基本信息 END ==========

    // ========== 领取规则 BEGIN ==========
    /**
     * 发放数量
     *
     * -1 - 则表示不限制发放数量
     */
    private Integer totalCount;
    /**
     * 每人限领个数
     *
     * -1 - 则表示不限制
     */
    private Integer takeLimitCount;
    /**
     * 领取方式
     *
     * 枚举 {@link CouponTakeTypeEnum}
     */
    private Integer takeType;
    // ========== 领取规则 END ==========

    // ========== 使用规则 BEGIN ==========
    /**
     * 是否设置满多少金额可用，单位：分
     *
     * 0 - 不限制
     * 大于 0 - 多少金额可用
     */
    private Integer usePrice;
    /**
     * 商品范围
     *
     * 枚举 {@link PromotionProductScopeEnum}
     */
    private Integer productScope;
    /**
     * 商品范围编号的数组
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> productScopeValues;
    /**
     * 生效日期类型
     *
     * 枚举 {@link CouponTemplateValidityTypeEnum}
     */
    private Integer validityType;
    /**
     * 固定日期 - 生效开始时间
     *
     * 当 {@link #validityType} 为 {@link CouponTemplateValidityTypeEnum#DATE}
     */
    private LocalDateTime validStartTime;
    /**
     * 固定日期 - 生效结束时间
     *
     * 当 {@link #validityType} 为 {@link CouponTemplateValidityTypeEnum#DATE}
     */
    private LocalDateTime validEndTime;
    /**
     * 领取日期 - 开始天数
     *
     * 当 {@link #validityType} 为 {@link CouponTemplateValidityTypeEnum#TERM}
     */
    private Integer fixedStartTerm;
    /**
     * 领取日期 - 结束天数
     *
     * 当 {@link #validityType} 为 {@link CouponTemplateValidityTypeEnum#TERM}
     */
    private Integer fixedEndTerm;
    // ========== 使用规则 END ==========

    // ========== 使用效果 BEGIN ==========
    /**
     * 折扣类型
     *
     * 枚举 {@link PromotionDiscountTypeEnum}
     */
    private Integer discountType;
    /**
     * 折扣百分比
     *
     * 例如，80% 为 80
     */
    private Integer discountPercent;
    /**
     * 优惠金额，单位：分
     *
     * 当 {@link #discountType} 为 {@link PromotionDiscountTypeEnum#PRICE} 生效
     */
    private Integer discountPrice;
    /**
     * 折扣上限，仅在 {@link #discountType} 等于 {@link PromotionDiscountTypeEnum#PERCENT} 时生效
     *
     * 例如，折扣上限为 20 元，当使用 8 折优惠券，订单金额为 1000 元时，最高只可折扣 20 元，而非 80  元。
     */
    private Integer discountLimitPrice;
    // ========== 使用效果 END ==========

    // ========== 统计信息 BEGIN ==========
    /**
     * 领取优惠券的数量
     */
    private Integer takeCount;
    /**
     * 使用优惠券的次数
     */
    private Integer useCount;

    // ========== 统计信息 END ==========

    // TODO 芋艿：领取开始时间、领取结束时间

}
