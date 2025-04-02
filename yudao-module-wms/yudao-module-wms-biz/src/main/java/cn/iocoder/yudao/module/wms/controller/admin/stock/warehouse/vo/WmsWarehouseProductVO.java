package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: LeeFJ
 * @date: 2025/4/2 8:41
 * @description:
 */
@Schema(description = "管理后台 - 仓库产品 VO")
@Data
@ExcelIgnoreUnannotated
public class WmsWarehouseProductVO {

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14491")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3153")
    @ExcelProperty("产品ID")
    private Long productId;




}
