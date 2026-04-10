package cn.iocoder.yudao.module.promotion.dal.dataobject.combination;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 拼团商品 DO
 *
 * @author HUIHUI
 */
@TableName("promotion_combination_product")
@KeySequence("promotion_combination_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombinationProductDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 拼团活动编号
     */
    private Long activityId;
    /**
     * 商品 SPU 编号
     */
    private Long spuId;
    /**
     * 商品 SKU 编号
     */
    private Long skuId;
    /**
     * 拼团价格，单位分
     */
    private Integer combinationPrice;

    /**
     * 拼团商品状态
     *
     * 关联 {@link CombinationActivityDO#getStatus()}
     */
    private Integer activityStatus;
    /**
     * 活动开始时间点
     *
     * 冗余 {@link CombinationActivityDO#getStartTime()}
     */
    private LocalDateTime activityStartTime;
    /**
     * 活动结束时间点
     *
     * 冗余 {@link CombinationActivityDO#getEndTime()}
     */
    private LocalDateTime activityEndTime;

}
