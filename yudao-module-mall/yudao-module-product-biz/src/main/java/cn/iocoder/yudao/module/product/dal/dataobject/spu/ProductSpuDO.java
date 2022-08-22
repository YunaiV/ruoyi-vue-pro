package cn.iocoder.yudao.module.product.dal.dataobject.spu;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.product.dal.dataobject.brand.ProductBrandDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
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
@TableName(value = "product_spu", autoResultMap = true)
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

    // ========== 基本信息 =========

    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品编码
     */
    private String code;
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
     * 商品图片的数组
     *
     * 1. 第一张图片将作为商品主图，支持同时上传多张图；
     * 2. 建议使用尺寸 800x800 像素以上、大小不超过 1M 的正方形图片；
     * 3. 至少 1 张，最多上传 10 张
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> picUrls;
    /**
     * 商品视频
     */
    private String videoUrl;

    /**
     * 排序字段
     */
    private Integer sort;
    /**
     * 商品状态
     *
     * 枚举 {@link ProductSpuStatusEnum}
     */
    private Integer status;

    // ========== SKU 相关字段 =========

    /**
     * 规格类型
     *
     * 枚举 {@link ProductSpuSpecTypeEnum}
     */
    private Integer specType;
    /**
     * 最小价格，单位使用：分
     *
     * 基于其对应的 {@link ProductSkuDO#getPrice()} 最小值
     */
    private Integer minPrice;
    /**
     * 最大价格，单位使用：分
     *
     * 基于其对应的 {@link ProductSkuDO#getPrice()} 最大值
     */
    private Integer maxPrice;
    /**
     * 市场价，单位使用：分
     *
     * 基于其对应的 {@link ProductSkuDO#getMarketPrice()} 最大值
     */
    private Integer marketPrice;
    /**
     * 总库存
     *
     * 基于其对应的 {@link ProductSkuDO#getStock()} 求和
     */
    private Integer totalStock;
    /**
     * 是否展示库存
     */
    private Boolean showStock;

    // ========== 统计相关字段 =========

    /**
     * 商品销量
     */
    private Integer salesCount;
    /**
     * 虚拟销量
     */
    private Integer virtualSalesCount;
    /**
     * 商品点击量
     */
    private Integer clickCount;

    // ========== 物流相关字段 =========

    // TODO 芋艿：稍后完善物流的字段
//    /**
//     * 配送方式
//     *
//     * 枚举 {@link DeliveryModeEnum}
//     */
//    private Integer deliveryMode;
//    /**
//     * 配置模板编号
//     *
//     * 关联 {@link DeliveryTemplateDO#getId()}
//     */
//    private Long deliveryTemplateId;

    // TODO ========== 待定字段：yv =========
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
    // TODO activity 活动显示排序 0=默认 1=秒 2=砍价 3=拼团

    // TODO ========== 待定字段：lf =========

    // TODO free_shipping_type：运费类型：1-包邮；2-统一运费；3-运费模板
    // TODO free_shipping：统一运费金额
    // TODO free_shipping_template_id：运费模板
    // TODO is_commission：分销佣金：1-开启；0-不开启；first_ratio second_ratio three_ratio
    // TODO is_share_bouns：区域股东分红：1-开启；0-不开启；region_ratio；shareholder_ratio

    // TODO is_new：新品推荐：1-是；0-否
    // TODO is_best：好物优选：1-是；0-否
    // TODO is_like：猜你喜欢：1-是；0-否

    // TODO is_team：是否开启拼团[0=否， 1=是]
    // TODO is_integral：积分抵扣：1-开启；0-不开启
    // TODO is_member：会员价：1-开启；0-不开启
    // TODO give_integral_type：赠送积分类型：0-不赠送；1-赠送固定积分；2-按比例赠送积分
    // TODO give_integral：赠送积分；

    // TODO poster：商品自定义海报

    // TODO ========== 待定字段：laoji =========
    // TODO productType 1 - 普通商品 2 - 预售商品；可能和 type 合并不错
    // TODO productUnit 商品单位
    // TODO extJson 扩展信息；例如说，预售商品的信息

}
