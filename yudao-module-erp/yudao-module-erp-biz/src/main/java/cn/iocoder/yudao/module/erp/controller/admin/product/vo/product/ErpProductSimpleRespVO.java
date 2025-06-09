package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ErpProductSimpleRespVO {
    @Schema(description = "产品编号")
    private Long id;

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "SKU（编码）")
    private String code;

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

    //TODO 包装类型，待优化
    @Schema(description = "包装长度（整数，没有小数点，单位mm，必须为正数）", example = "500")
    private Integer packageLength;

    @Schema(description = "包装宽度（整数，没有小数点，单位mm，必须为正数）", example = "300")
    private Integer packageWidth;

    @Schema(description = "包装高度（整数，没有小数点，单位mm，必须为正数）", example = "200")
    private Integer packageHeight;

    @Schema(description = "包装重量（保留至小数点后两位，单位kg，必须为非负数）", example = "12.50")
    private BigDecimal packageWeight;

    @Schema(description = "基础重量（kg）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal weight;
}
