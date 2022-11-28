package cn.iocoder.yudao.module.promotion.dal.dataobject.seckillactivity;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 秒杀参与商品
 * @TableName promotion_seckill_product
 */
@TableName(value ="promotion_seckill_product")
@Data
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
    private Long timeId;

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