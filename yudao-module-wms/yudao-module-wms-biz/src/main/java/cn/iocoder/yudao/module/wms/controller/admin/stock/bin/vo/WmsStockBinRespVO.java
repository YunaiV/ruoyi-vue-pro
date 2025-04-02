package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo;

import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,update_time,create_time,outbound_pending_qty,bin_id,product_id,available_qty,id,sellable_qty,updater,warehouse_id
 */
@Schema(description = "管理后台 - 仓位库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockBinRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30764")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "748")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "仓库", example = "")
    private WmsWarehouseSimpleRespVO warehouse;

    @Schema(description = "库位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10839")
    @ExcelProperty("库位ID")
    private Long binId;

    @Schema(description = "库位", example = "")
    private WmsWarehouseBinRespVO bin;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11713")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "产品", example = "")
    private WmsProductRespSimpleVO product;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "可用量，在库的良品数量", example = "")
    @ExcelProperty("可用量，在库的良品数量")
    private Integer availableQty;

    @Schema(description = "待出库量", example = "")
    @ExcelProperty("待出库量")
    private Integer outboundPendingQty;

    @Schema(description = "可售量，未被单据占用的良品数量", example = "")
    @ExcelProperty("可售量，未被单据占用的良品数量")
    private Integer sellableQty;
}
