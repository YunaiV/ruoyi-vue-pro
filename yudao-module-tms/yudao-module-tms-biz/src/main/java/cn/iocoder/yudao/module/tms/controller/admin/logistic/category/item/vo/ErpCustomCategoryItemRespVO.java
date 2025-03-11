package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 海关分类子表 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpCustomCategoryItemRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5710")
    @ExcelProperty("编号")
    private Long id;

//    @Schema(description = "分类表id", requiredMode = Schema.RequiredMode.REQUIRED, example = "25022")
//    @ExcelProperty("分类表id")
//    private Integer categoryId;

    @Schema(description = "国家-字典", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("国家-字典")
    private Integer countryCode;

    @Schema(description = "HS编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("HS编码")
    private String hscode;

    @Schema(description = "税率", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("税率")
    private BigDecimal taxRate;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间")
    @ExcelProperty("最后更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "最后更新人")
    @ExcelProperty("最后更新人")
    private String updater;
}