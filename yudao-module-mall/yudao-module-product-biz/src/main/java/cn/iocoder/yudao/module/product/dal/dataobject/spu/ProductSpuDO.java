package cn.iocoder.yudao.module.product.dal.dataobject.spu;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.dal.dataobject.brand.ProductBrandDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.delivery.DeliveryTemplateDO;
import cn.iocoder.yudao.module.product.dal.dataobject.shop.ShopDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.enums.delivery.DeliveryModeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 商品 SPU DO
 *
 * @author 芋道源码
 */
@TableName("product_spu")
@KeySequence("product_spu_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpuDO extends BaseDO {

    /**
     * 商品 SPU 编号，自增
     */
    @TableId
    private Long id;

//    /**
//     * 店铺编号
//     *
//     * 关联 {@link ShopDO#getId()} TODO 芋艿：多店铺，暂不考虑
//     */
//    private Long shopId;

    // ========== 基本信息 =========

    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品卖点
     */
    private String sellPoint;
    /**
     * 商品详情
     */
    private String description;
    /**
     * 商品分类编号
     *
     * 关联 {@link ProductCategoryDO#getId()}
     */
    private Long categoryId;
    /**
     * 商品品牌编号
     *
     * 关联 {@link ProductBrandDO#getId()}
     */
    private Long brandId;
    /**
     * 商品图片地址数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> picUrls;
    /**
     * 排序字段
     */
    private Integer sort;
    /**
     * 商品状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    // ========== SKU 相关字段 =========

    /**
     * 价格，单位使用：分
     *
     * 基于其对应的 {@link ProductSkuDO#getPrice()} 最小值
     */
    private Integer price;
    /**
     * 总库存
     *
     * 基于其对应的 {@link ProductSkuDO#getActualStocks()} 求和
     */
    private Integer totalStocks;
    /**
     * 已销售数量
     */
    private Integer soldNum;

    // ========== 物流相关字段 =========

    /**
     * 配送方式
     *
     * 枚举 {@link DeliveryModeEnum}
     */
    private Integer deliveryMode;
    /**
     * 配置模板编号
     *
     * 关联 {@link DeliveryTemplateDO#getId()}
     */
    private Long deliveryTemplateId;

}
