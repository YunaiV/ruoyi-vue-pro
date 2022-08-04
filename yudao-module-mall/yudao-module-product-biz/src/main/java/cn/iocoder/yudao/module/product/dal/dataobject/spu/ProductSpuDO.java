package cn.iocoder.yudao.module.product.dal.dataobject.spu;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.dal.dataobject.brand.ProductBrandDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.delivery.DeliveryTemplateDO;
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

    // ========== 统计相关字段 =========

    /**
     * 已销售数量（真实）
     */
    private Integer soldCount;
    /**
     * 浏览量
     */
    private Integer visitCount;

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

    // TODO ========== 待定字段：yv =========
    // TODO bar_code 条形码
    // TODO vip_price 会员价格
    // TODO postage 邮费
    // TODO is_postage 是否包邮
    // TODO unit_name 单位
    // TODO is_new 商户是否代理
    // TODO give_integral 获得积分
    // TODO is_integral 是开启积分兑换
    // TODO integral 所需积分
    // TODO is_seckill 秒杀状态
    // TODO is_bargain 砍价状态
    // TODO ficti 虚拟销量
    // TODO code_path 产品二维码地址
    // TODO is_sub 是否分佣

    // TODO ↓↓ 芋艿 ↓↓ 看起来走分组更合适？
    // TODO is_hot 是否热卖
    // TODO is_benefit 是否优惠
    // TODO is_best 是否精品
    // TODO is_new 是否新品
    // TODO is_good 是否优品推荐

    // TODO ========== 待定字段：cf =========
    // TODO source_link 淘宝京东1688类型
    // TODO video_link 主图视频链接
    // TODO activity 活动显示排序 0=默认 1=秒 2=砍价 3=拼团

}
