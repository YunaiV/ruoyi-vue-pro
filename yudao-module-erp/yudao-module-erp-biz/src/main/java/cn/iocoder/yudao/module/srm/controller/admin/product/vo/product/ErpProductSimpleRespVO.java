package cn.iocoder.yudao.module.srm.controller.admin.product.vo.product;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ErpProductSimpleRespVO {
    @Schema(description = "产品编号")
    private Long id;

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "SKU（编码）")
    private String barCode;

    @Schema(description = "产品分类编号")
    private Long categoryId;

    @Schema(description = "产品分类名称")
    private String categoryName;

    @Schema(description = "品牌")
    @ExcelProperty("品牌")
    private String brand;

    @Schema(description = "系列")
    @ExcelProperty("系列")
    private String series;

    @Schema(description = "单位编号")
    @ExcelProperty("单位编号")
    private Long unitId;

    @Schema(description = "单位名称")
    @ExcelProperty("单位名称")
    private String unitName;
}
