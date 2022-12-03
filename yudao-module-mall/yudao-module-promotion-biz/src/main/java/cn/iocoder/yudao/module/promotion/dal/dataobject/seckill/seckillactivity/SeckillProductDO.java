package cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 秒杀参与商品
 *
 * @author halfninety
 * @TableName promotion_seckill_product
 */
@TableName(value = "promotion_seckill_product", autoResultMap = true)
@KeySequence("promotion_seckill_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillProductDO extends BaseDO {
    /**
     * 秒杀参与商品编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 秒杀活动id
     */
    private Long activityId;

    /**
     * 秒杀时段id
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> timeIds;

    /**
     * 商品id
     */
    private Long spuId;

    /**
     * 商品sku_id
     */
    private Long skuId;

    /**
     * 秒杀金额
     */
    private Integer seckillPrice;

    /**
     * 秒杀库存
     */
    private Integer stock;

    /**
     * 每人限购
     */
    private Integer limitBuyCount;
}