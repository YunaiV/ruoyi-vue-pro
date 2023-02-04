package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import cn.iocoder.yudao.module.product.controller.app.property.vo.value.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 商品 SPU 明细 Response VO")
@Data
public class AppProductSpuDetailRespVO {

    @Schema(description = "商品 SPU 编号", required = true, example = "1")
    private Long id;

    // ========== 基本信息 =========

    @Schema(description = "商品名称", required = true, example = "芋道")
    private String name;

    @Schema(description = "促销语", example = "好吃！")
    private String sellPoint;

    @Schema(description = "商品详情", required = true, example = "我是商品描述")
    private String description;

    @Schema(description = "商品分类编号", required = true, example = "1")
    private Long categoryId;

    @Schema(description = "商品图片的数组", required = true)
    private List<String> picUrls;

    @Schema(description = "商品视频", required = true)
    private String videoUrl;

    // ========== SKU 相关字段 =========

    @Schema(description = "规格类型", required = true, example = "1")
    private Integer specType;

    @Schema(description = "是否展示库存", required = true, example = "true")
    private Boolean showStock;

    @Schema(description = " 最小价格，单位使用：分", required = true, example = "1024")
    private Integer minPrice;

    @Schema(description = "最大价格，单位使用：分", required = true, example = "1024")
    private Integer maxPrice;

    /**
     * SKU 数组
     */
    private List<Sku> skus;

    // ========== 统计相关字段 =========

    @Schema(description = "商品销量", required = true, example = "1024")
    private Integer salesCount;

    @Schema(description = "用户 App - 商品 SPU 明细的 SKU 信息")
    @Data
    public static class Sku {

        @Schema(description = "商品 SKU 编号", example = "1")
        private Long id;

        /**
         * 商品属性数组
         */
        private List<AppProductPropertyValueDetailRespVO> properties;

        @Schema(description = "销售价格，单位：分", required = true, example = "1024")
        private Integer price;

        @Schema(description = "市场价", example = "1024")
        private Integer marketPrice;

        @Schema(description = "图片地址", required = true, example = "https://www.iocoder.cn/xx.png")
        private String picUrl;

        @Schema(description = "库存", required = true, example = "1")
        private Integer stock;

        @Schema(description = "商品重量", example = "1") // 单位：kg 千克
        private Double weight;

        @Schema(description = "商品体积", example = "1024") // 单位：m^3 平米
        private Double volume;
    }

}
