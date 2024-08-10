package cn.iocoder.yudao.module.product.api.spu.dto;

import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import lombok.Data;

/**
 * 商品 SPU 信息 Response DTO
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Data
public class ProductSpuRespDTO {

    /**
     * 商品 SPU 编号，自增
     */
    private Long id;

    // ========== 基本信息 =========

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品分类编号
     */
    private Long categoryId;
    /**
     * 商品封面图
     */
    private String picUrl;

    /**
     * 商品状态
     * <p>
     * 枚举 {@link ProductSpuStatusEnum}
     */
    private Integer status;

    // ========== SKU 相关字段 =========

    /**
     * 规格类型
     *
     * false - 单规格
     * true - 多规格
     */
    private Boolean specType;
    /**
     * 商品价格，单位使用：分
     */
    private Integer price;
    /**
     * 市场价，单位使用：分
     */
    private Integer marketPrice;
    /**
     * 成本价，单位使用：分
     */
    private Integer costPrice;
    /**
     * 库存
     */
    private Integer stock;

    // ========== 物流相关字段 =========

    /**
     * 物流配置模板编号
     *
     * 对应 TradeDeliveryExpressTemplateDO 的 id 编号
     */
    private Long deliveryTemplateId;

    // ========== 营销相关字段 =========

    /**
     * 赠送积分
     */
    private Integer giveIntegral;

    // ========== 分销相关字段 =========

    /**
     * 分销类型
     *
     * false - 默认
     * true - 自行设置
     */
    private Boolean subCommissionType;

}
