package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo;

import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,create_time,outbound_pending_qty,transit_qty,available_qty,make_pending_qty,updater,update_time,product_id,shelving_pending_qty,id,defective_qty,return_transit_qty,sellable_qty,warehouse_id
 */
@Schema(description = "管理后台 - 仓库库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockWarehouseRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21341")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14491")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3153")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

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
    @ExcelProperty("可用量")
    private Integer availableQty;

    @Schema(description = "不良品数量", example = "")
    @ExcelProperty("不良品数量")
    private Integer defectiveQty;

    @Schema(description = "待出库量", example = "")
    @ExcelProperty("待出库量")
    private Integer outboundPendingQty;

    @Schema(description = "退件在途数量", example = "")
    @ExcelProperty("退件在途数量")
    private Integer returnTransitQty;

    @Schema(description = "可售量，未被单据占用的良品数量", example = "")
    @ExcelProperty("可售量")
    private Integer sellableQty;

    @Schema(description = "待上架数量，上架是指从拣货区上架到货架", example = "")
    @ExcelProperty("待上架数量")
    private Integer shelvingPendingQty;

    @Schema(description = "仓库", example = "")
    private WmsWarehouseSimpleRespVO warehouse;

    @Schema(description = "产品", example = "")
    private WmsProductRespSimpleVO product;

    @Schema(description = "仓位库存", example = "")
    private List<WmsStockBinRespVO> stockBinList;

    @Schema(description = "在途量", example = "")
    @ExcelProperty("在途量")
    private Integer transitQty;

    @Schema(description = "在制数量", example = "")
    @ExcelProperty("在制数量")
    private Integer makePendingQty;
}
