package cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.item;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - WMS 商品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsItemRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "商品编号", example = "ITEM001")
    @ExcelProperty("商品编号")
    private String code;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "华为 nova flip")
    @ExcelProperty("商品名称")
    private String name;

    @Schema(description = "商品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long categoryId;

    @Schema(description = "商品分类名称", example = "手机")
    @ExcelProperty("商品分类")
    private String categoryName;

    @Schema(description = "单位", example = "台")
    @ExcelProperty("单位")
    private String unit;

    @Schema(description = "商品品牌编号", example = "1")
    private Long brandId;

    @Schema(description = "商品品牌名称", example = "华为")
    @ExcelProperty("商品品牌")
    private String brandName;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "规格列表")
    private List<WmsItemSkuRespVO> skus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
