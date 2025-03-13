package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 海关产品分类表 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpCustomProductRespVO {
    @Schema(description = "id")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "产品id", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品id")
    private Long productId;

    @Schema(description = "海关分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("海关分类id")
    private Long customCategoryId;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    @ExcelProperty("更新人")
    private String updater;

    @Schema(description = "更新时间")
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "产品信息")
    private ErpProductDTO product;

    @Schema(description = "材质对应string+报关品名")
    @ExcelProperty("材质对应string+报关品名")
    private String combinedValue;
    @Schema(description = "海关分类信息")
    private ErpCustomCategoryDO customCategory;
}