package cn.iocoder.yudao.module.product.dal.dataobject.sku;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 商品 SKU DO
 *
 * @author 芋道源码
 */
@TableName(value = "product_sku",autoResultMap = true)
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
     * SPU 编号
     * <p>
     * 关联 {@link ProductSpuDO#getId()}
     */
    private Long spuId;
    /**
     * SPU 名字
     *
     * 冗余 {@link ProductSkuDO#getSpuName()}
     */
    private String spuName;
    /**
     * 属性数组，JSON 格式
     */
    @TableField(typeHandler = PropertyTypeHandler.class)
    private List<Property> properties;
    /**
     * 销售价格，单位：分
     */
    private Integer price;
    /**
     * 市场价，单位：分
     */
    private Integer marketPrice;
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
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 库存
     */
    private Integer stock;
    /**
     * 预警预存
     */
    private Integer warnStock;
    /**
     * 商品重量，单位：kg 千克
     */
    private Double weight;
    /**
     * 商品体积，单位：m^3 平米
     */
    private Double volume;

    /**
     * 商品属性
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Property {

        /**
         * 属性编号
         * <p>
         * 关联 {@link ProductPropertyDO#getId()}
         */
        private Long propertyId;
        /**
         * 属性值编号
         * <p>
         * 关联 {@link ProductPropertyValueDO#getId()}
         */
        private Long valueId;

    }

    // TODO @芋艿：可以找一些新的思路
    public static class PropertyTypeHandler extends AbstractJsonTypeHandler<Object> {

        @Override
        protected Object parse(String json) {
            return JsonUtils.parseArray(json, Property.class);
        }

        @Override
        protected String toJson(Object obj) {
            return JsonUtils.toJsonString(obj);
        }

    }

}

