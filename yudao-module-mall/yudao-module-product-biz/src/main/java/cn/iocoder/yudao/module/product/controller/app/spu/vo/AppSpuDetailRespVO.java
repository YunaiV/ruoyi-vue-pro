package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import cn.iocoder.yudao.module.product.controller.app.property.vo.value.AppProductPropertyValueDetailRespVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("用户 App - 商品 SPU 明细 Response VO")
@Data
public class AppSpuDetailRespVO {

    @ApiModelProperty(value = "商品 SPU 编号", required = true, example = "1")
    private Long id;

    // ========== 基本信息 =========

    @ApiModelProperty(value = "商品名称", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = "促销语", example = "好吃！")
    private String sellPoint;

    @ApiModelProperty(value = "商品详情", required = true, example = "我是商品描述")
    private String description;

    @ApiModelProperty(value = "商品分类编号", required = true, example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "商品图片的数组", required = true)
    private List<String> picUrls;

    @ApiModelProperty(value = "商品视频", required = true)
    private String videoUrl;

    // ========== SKU 相关字段 =========

    @ApiModelProperty(value = "规格类型", required = true, example = "1", notes = "参见 ProductSpuSpecTypeEnum 枚举类")
    private Integer specType;

    @ApiModelProperty(value = "是否展示库存", required = true, example = "true")
    private Boolean showStock;

    @ApiModelProperty(value = " 最小价格，单位使用：分", required = true, example = "1024")
    private Integer minPrice;

    @ApiModelProperty(value = "最大价格，单位使用：分", required = true, example = "1024")
    private Integer maxPrice;

    /**
     * SKU 数组
     */
    private List<Sku> skus;

    // ========== 统计相关字段 =========

    @ApiModelProperty(value = "商品销量", required = true, example = "1024")
    private Integer salesCount;

    @ApiModel("用户 App - 商品 SPU 明细的 SKU 信息")
    @Data
    public static class Sku {

        @ApiModelProperty(value = "商品 SKU 编号", example = "1")
        private Long id;

        /**
         * 商品属性数组
         */
        private List<AppProductPropertyValueDetailRespVO> properties;

        @ApiModelProperty(value = "销售价格，单位：分", required = true, example = "1024", notes = "单位：分")
        private Integer price;

        @ApiModelProperty(value = "市场价", example = "1024", notes = "单位：分")
        private Integer marketPrice;

        @ApiModelProperty(value = "图片地址", required = true, example = "https://www.iocoder.cn/xx.png")
        private String picUrl;

        @ApiModelProperty(value = "库存", required = true, example = "1")
        private Integer stock;

        @ApiModelProperty(value = "商品重量", example = "1", notes = "单位：kg 千克")
        private Double weight;

        @ApiModelProperty(value = "商品体积", example = "1024", notes = "单位：m^3 平米")
        private Double volume;
    }

}
