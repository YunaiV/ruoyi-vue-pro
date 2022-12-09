package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title = "管理后台 - 商品 SKU 选项 Response VO", description = "用于前端 SELECT 选项")
@Data
public class ProductSkuOptionRespVO {

    @Schema(title = "主键", required = true, example = "1024")
    private Long id;

    @Schema(title = "商品 SKU 名字", example = "红色")
    private String name;

    @Schema(title = "销售价格", required = true, example = "100", description = "单位：分")
    private String price;

    @Schema(title = "库存", required = true, example = "100")
    private Integer stock;

    // ========== 商品 SPU 信息 ==========

    @Schema(title = "商品 SPU 编号", required = true, example = "1")
    private Long spuId;

    @Schema(title = "商品 SPU 名字", required = true, example = "iPhone 11")
    private String spuName;

}
