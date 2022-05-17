package cn.iocoder.yudao.module.product.dal.dataobject.sku;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

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
public class SkuDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * spu编号
     */
    private Integer spuId;
    /**
     * 状态： 1-正常 2-禁用
     */
    private Integer skuStatus;
    /**
     * 规格值数组， 以逗号隔开
     */
    private String attrs;
    /**
     * 销售价格，单位：分
     */
    private Integer price;
    /**
     * 原价， 单位： 分
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

}
