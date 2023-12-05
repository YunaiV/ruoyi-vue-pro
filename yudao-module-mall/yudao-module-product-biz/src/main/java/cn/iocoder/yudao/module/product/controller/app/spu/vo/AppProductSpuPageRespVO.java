package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 商品 SPU Response VO")
@Data
public class AppProductSpuPageRespVO {

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String name;

    @Schema(description = "商品简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖简介")
    private String introduction;

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @Schema(description = "商品封面图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String picUrl;

    @Schema(description = "商品轮播图", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> sliderPicUrls;

    @Schema(description = "单位名", requiredMode = Schema.RequiredMode.REQUIRED, example = "个")
    private String unitName;

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

    // ========== 营销相关字段 =========

    @Schema(description = "活动排序数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private List<Integer> activityOrders;

    // ========== 统计相关字段 =========

    @Schema(description = "商品销量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer salesCount;

}
