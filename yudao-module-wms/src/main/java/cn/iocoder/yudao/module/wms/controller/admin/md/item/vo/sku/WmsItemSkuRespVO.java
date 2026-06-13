package cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 商品 SKU Response VO")
@Data
public class WmsItemSkuRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "规格名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "黑色")
    private String name;

    @Schema(description = "商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "商品编码", example = "ITEM001")
    private String itemCode;

    @Schema(description = "商品名称", example = "华为 nova flip")
    private String itemName;

    @Schema(description = "商品分类编号", example = "1")
    private Long categoryId;

    @Schema(description = "商品分类名称", example = "手机")
    private String categoryName;

    @Schema(description = "单位", example = "台")
    private String unit;

    @Schema(description = "商品品牌编号", example = "1")
    private Long brandId;

    @Schema(description = "商品品牌名称", example = "华为")
    private String brandName;

    @Schema(description = "条码", example = "690000000001")
    private String barCode;

    @Schema(description = "规格编号", example = "SKU001")
    private String code;

    @Schema(description = "长，单位 cm", example = "10.0")
    private BigDecimal length;

    @Schema(description = "宽，单位 cm", example = "8.0")
    private BigDecimal width;

    @Schema(description = "高，单位 cm", example = "1.0")
    private BigDecimal height;

    @Schema(description = "毛重，单位 kg", example = "1.000")
    private BigDecimal grossWeight;

    @Schema(description = "净重，单位 kg", example = "0.900")
    private BigDecimal netWeight;

    @Schema(description = "成本价，单位元", example = "5000.00")
    private BigDecimal costPrice;

    @Schema(description = "销售价，单位元", example = "5288.00")
    private BigDecimal sellingPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
