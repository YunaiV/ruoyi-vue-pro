package cn.iocoder.yudao.module.promotion.dal.dataobject.bargain;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 砍价商品 DO
 *
 * @author HUIHUI
 */
@Deprecated // 应该融合到 BargainActivityDO 表
@TableName("promotion_bargain_product")
@KeySequence("promotion_bargain_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BargainProductDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 砍价活动编号
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
     * 砍价商品状态
     *
     * 关联 {@link BargainActivityDO#getStatus()}
     */
    private Integer activityStatus;
    /**
     * 活动开始时间点
     */
    private LocalDateTime activityStartTime;
    /**
     * 活动结束时间点
     */
    private LocalDateTime activityEndTime;
    /**
     * 砍价起始价格，单位分
     */
    private Integer bargainFirstPrice;
    /**
     * 砍价底价，单位：分
     */
    private Integer bargainPrice;
    /**
     * 活动库存
     */
    private Integer stock;

}
