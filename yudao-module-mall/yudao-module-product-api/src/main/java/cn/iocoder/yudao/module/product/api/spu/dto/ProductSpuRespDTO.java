package cn.iocoder.yudao.module.product.api.spu.dto;

import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import lombok.Data;

import java.util.List;

// TODO @LeeYan9: ProductSpuRespDTO
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
     * 商品编码
     */
    private String code;
    /**
     * 促销语
     */
    private String sellPoint;
    /**
     * 商品详情
     */
    private String description;
    /**
     * 商品分类编号
     */
    private Long categoryId;
    /**
     * 商品品牌编号
     */
    private Long brandId;
    /**
     * 商品图片的数组
     * <p>
     * 1. 第一张图片将作为商品主图，支持同时上传多张图；
     * 2. 建议使用尺寸 800x800 像素以上、大小不超过 1M 的正方形图片；
     * 3. 至少 1 张，最多上传 10 张
     */
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
     * <p>
     * 枚举 {@link ProductSpuStatusEnum}
     */
    private Integer status;

    // ========== SKU 相关字段 =========

    /**
     * 规格类型
     * <p>
     * 枚举 {@link ProductSpuSpecTypeEnum}
     */
    private Integer specType;
    /**
     * 最小价格，单位使用：分
     * <p>
     * 基于其对应的 {@link ProductSkuRespDTO#getPrice()} 最小值
     */
    private Integer minPrice;
    /**
     * 最大价格，单位使用：分
     * <p>
     * 基于其对应的 {@link ProductSkuRespDTO#getPrice()} 最大值
     */
    private Integer maxPrice;
    /**
     * 市场价，单位使用：分
     * <p>
     * 基于其对应的 {@link ProductSkuRespDTO#getMarketPrice()} 最大值
     */
    private Integer marketPrice;
    /**
     * 总库存
     * <p>
     * 基于其对应的 {@link ProductSkuRespDTO#getStock()} 求和
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

}
