package cn.iocoder.yudao.module.product.dal.dataobject.sku;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 商品 SKU DO
 *
 * @author 芋道源码
 */
@TableName("product_sku")
@KeySequence("product_sku_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuDO extends BaseDO {

    /**
     * 商品 SKU 编号，自增
     */
    @TableId
    private Long id;
    /**
     * 商品 SKU 名字
     */
    private String name;
    /**
     * SPU 编号
     *
     * 关联 {@link ProductSpuDO#getId()}
     */
    private Long spuId;
    /**
     * 规格值数组，JSON 格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Property> properties;
    /**
     * 销售价格，单位：分
     */
    private Integer price;
    /**
     * 原价，单位：分
     */
    private Integer originalPrice;
    /**
     * 成本价，单位：分
     */
    private Integer costPrice;
    /**
     * SKU 的条形码
     */
    private String barCode;
    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * SKU 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 购买中的库存
     *
     * 商品 SKU 被下单时，未付款的商品 SKU 数量
     */
    private Integer stocks;
    /**
     * 实际库存
     */
    private Integer actualStocks;
    /**
     * 商品重量，单位：kg 千克
     */
    private Double weight;
    /**
     * 商品体积，单位：m^3 平米
     */
    private Double volume;

    @Data
    public static class Property {

        /**
         * 属性编号
         *
         * 关联 {@link ProductPropertyDO#getId()}
         */
        private Long propertyId;
        /**
         * 属性值编号
         *
         * 关联 {@link ProductPropertyValueDO#getId()}
         */
        private Long valueId;

    }

    // TODO ========== 待定字段：yv =========
    // TODO brokerage：一级返佣
    // TODO brokerage_two：二级返佣
    // TODO pink_price：拼团价
    // TODO pink_stock：拼团库存
    // TODO seckill_price：秒杀价
    // TODO seckill_stock：秒杀库存
    // TODO integral：需要积分

}

