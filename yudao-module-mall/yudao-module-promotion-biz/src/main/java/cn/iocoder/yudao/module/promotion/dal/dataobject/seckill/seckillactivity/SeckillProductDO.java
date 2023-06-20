package cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 秒杀参与商品 DO
 *
 * @author HUIHUI
 */
@TableName("promotion_seckill_product")
@KeySequence("promotion_seckill_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillProductDO extends BaseDO {

    /**
     * 秒杀参与商品编号
     */
    @TableId
    private Long id;
    /**
     * 秒杀活动 id
     */
    private Long activityId;
    /**
     * 秒杀时段 id
     */
    private String configIds;
    /**
     * 商品 spu_id
     */
    private Long spuId;
    /**
     * 商品 sku_id
     */
    private Long skuId;
    /**
     * 秒杀金额，单位：分
     */
    private Integer seckillPrice;
    /**
     * 秒杀库存
     */
    private Integer stock;
    /**
     * 秒杀商品状态
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

}
