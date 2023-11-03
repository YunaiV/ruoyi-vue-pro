package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import cn.iocoder.yudao.module.product.controller.app.property.vo.value.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 商品 SPU 明细 Response VO")
@Data
public class AppProductSpuDetailRespVO {

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    // ========== 基本信息 =========

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String name;

    @Schema(description = "商品简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是一个快乐简介")
    private String introduction;

    @Schema(description = "商品详情", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是商品描述")
    private String description;

    @Schema(description = "商品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long categoryId;

    @Schema(description = "商品封面图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String picUrl;

    @Schema(description = "商品轮播图", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> sliderPicUrls;

    @Schema(description = "商品视频", requiredMode = Schema.RequiredMode.REQUIRED)
    private String videoUrl;

    @Schema(description = "单位名", requiredMode = Schema.RequiredMode.REQUIRED, example = "个")
    private String unitName;

    // ========== 营销相关字段 =========

    @Schema(description = "活动排序数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private List<Integer> activityOrders;

    // ========== SKU 相关字段 =========

    @Schema(description = "规格类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean specType;

    @Schema(description = "商品价格，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer price;

    @Schema(description = "市场价，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer marketPrice;

    @Schema(description = "VIP 价格，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "968") // 通过会员等级，计算出折扣后价格
    private Integer vipPrice;

    @Schema(description = "库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "666")
    private Integer stock;

    /**
     * SKU 数组
     */
    private List<Sku> skus;

    // ========== 统计相关字段 =========

    @Schema(description = "商品销量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
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

        @Schema(description = "销售价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Integer price;

        @Schema(description = "市场价，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Integer marketPrice;

        @Schema(description = "VIP 价格，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "968") // 通过会员等级，计算出折扣后价格
        private Integer vipPrice;

        @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.png")
        private String picUrl;

        @Schema(description = "库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer stock;

        @Schema(description = "商品重量", example = "1") // 单位：kg 千克
        private Double weight;

        @Schema(description = "商品体积", example = "1024") // 单位：m^3 平米
        private Double volume;

    }

}
