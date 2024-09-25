package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - ERP 供应商产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpSupplierProductRespVO {

    @Schema(description = "供应商产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "768")
    @ExcelProperty("供应商产品编号")
    private Long id;

    @Schema(description = "供应商产品编码")
    @ExcelProperty("供应商产品编码")
    private String code;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29689")
    @ExcelProperty("供应商编号")
    private Long supplierId;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26097")
    @ExcelProperty("产品编号")
    private Long productId;

    @Schema(description = "包装高度")
    @ExcelProperty("包装高度")
    private Double packageHeight;

    @Schema(description = "包装长度")
    @ExcelProperty("包装长度")
    private Double packageLength;

    @Schema(description = "包装重量")
    @ExcelProperty("包装重量")
    private Double packageWeight;

    @Schema(description = "包装宽度")
    @ExcelProperty("包装宽度")
    private Double packageWidth;

    @Schema(description = "采购价格", example = "25304")
    @ExcelProperty("采购价格")
    private Double purchasePrice;

    @Schema(description = "采购货币代码")
    @ExcelProperty("采购货币代码")
    private String purchasePriceCurrencyCode;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}