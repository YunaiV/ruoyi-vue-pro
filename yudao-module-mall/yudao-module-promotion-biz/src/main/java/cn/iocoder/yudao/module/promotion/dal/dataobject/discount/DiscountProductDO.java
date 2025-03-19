package cn.iocoder.yudao.module.promotion.dal.dataobject.discount;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 限时折扣商品 DO
 *
 * @author 芋道源码
 */
@TableName(value = "promotion_discount_product", autoResultMap = true)
@KeySequence("promotion_discount_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class DiscountProductDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 限时折扣活动的编号
     *
     * 关联 {@link DiscountActivityDO#getId()}
     */
    private Long activityId;

    /**
     * 商品 SPU 编号
     *
     * 关联 ProductSpuDO 的 id 编号
     */
    private Long spuId;
    /**
     * 商品 SKU 编号
     *
     * 关联 ProductSkuDO 的 id 编号
     */
    private Long skuId;

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
     * 活动标题
     *
     * 冗余 {@link DiscountActivityDO#getName()}
     */
    private String activityName;
    /**
     * 活动状态
     *
     * 冗余 {@link DiscountActivityDO#getStatus()}
     */
    private Integer activityStatus;
    /**
     * 活动开始时间点
     *
     * 冗余 {@link DiscountActivityDO#getStartTime()}
     */
    private LocalDateTime activityStartTime;
    /**
     * 活动结束时间点
     *
     * 冗余 {@link DiscountActivityDO#getEndTime()}
     */
    private LocalDateTime activityEndTime;

}
