package cn.iocoder.yudao.module.product.dal.dataobject.sku;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商品sku DO
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
     * 主键
     */
    @TableId
    private Long id;
    /**
     * spu编号
     */
    private Long spuId;
    /**
     * 规格值数组-json格式， [{propertId: , valueId: }, {propertId: , valueId: }]
     */
    // TODO franky：可以定义一个内部的 Property 类，然后 List<Property>
    private String properties;
    /**
     * 销售价格，单位：分
     */
    private Integer price;
    /**
     * 原价，单位：分
     */
    private Integer originalPrice;
    /**
     * 成本价，单位： 分
     */
    private Integer costPrice;
    /**
     * 条形码
     */
    private String barCode;
    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * 状态： 0-正常 1-禁用
     */
    private Integer status;

}

