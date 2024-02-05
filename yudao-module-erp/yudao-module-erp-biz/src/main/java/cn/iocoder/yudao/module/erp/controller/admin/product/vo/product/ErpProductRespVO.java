package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpProductRespVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15672")
    @ExcelProperty("产品编号")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "产品条码", requiredMode = Schema.RequiredMode.REQUIRED, example = "X110")
    @ExcelProperty("产品条码")
    private String barCode;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11161")
    private Long categoryId;
    @Schema(description = "产品分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "水果")
    @ExcelProperty("产品分类")
    private String categoryName;

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8869")
    private Long unitId;
    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "个")
    @ExcelProperty("单位")
    private String unitName;

    @Schema(description = "产品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("产品状态")
    private Integer status;

    @Schema(description = "产品规格", example = "红色")
    @ExcelProperty("产品规格")
    private String standard;

    @Schema(description = "产品备注", example = "你猜")
    @ExcelProperty("产品备注")
    private String remark;

    @Schema(description = "保质期天数", example = "10")
    @ExcelProperty("保质期天数")
    private Integer expiryDay;

    @Schema(description = "基础重量（kg）", example = "1.00")
    @ExcelProperty("基础重量（kg）")
    private BigDecimal weight;

    @Schema(description = "采购价格，单位：元", example = "10.30")
    @ExcelProperty("采购价格，单位：元")
    private BigDecimal purchasePrice;

    @Schema(description = "销售价格，单位：元", example = "74.32")
    @ExcelProperty("销售价格，单位：元")
    private BigDecimal salePrice;

    @Schema(description = "最低价格，单位：元", example = "161.87")
    @ExcelProperty("最低价格，单位：元")
    private BigDecimal minPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}