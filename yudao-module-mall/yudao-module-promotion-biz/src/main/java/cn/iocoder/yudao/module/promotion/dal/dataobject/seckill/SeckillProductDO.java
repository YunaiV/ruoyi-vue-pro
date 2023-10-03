package cn.iocoder.yudao.module.promotion.dal.dataobject.seckill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
     *
     * 关联 {@link SeckillActivityDO#getId()}
     */
    private Long activityId;
    /**
     * 秒杀时段 id
     *
     * 关联 {@link SeckillConfigDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> configIds;
    /**
     * 商品 SPU 编号
     */
    private Long spuId;
    /**
     * 商品 SKU 编号
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
     *
     * 枚举 {@link CommonStatusEnum 对应的类}
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
