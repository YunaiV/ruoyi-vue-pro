package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @table-fields : bill_id,tenant_id,outbound_available_qty,creator,create_time,updater,inbound_id,outbound_available_delta_qty,outbound_action_id,update_time,bill_item_id,product_id,bill_type,id,inbound_item_id,direction
 */
@Schema(description = "管理后台 - 入库单库存详情扣减 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundItemFlowSimpleVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13478")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23778")
    @ExcelProperty("入库单ID")
    private Long inboundId;

    @Schema(description = "单据类型", example = "")
    @ExcelProperty("单据类型")
    private Integer billType;

    @Schema(description = "出库单ID", example = "")
    @ExcelProperty("出库单ID")
    private Long billId;

    @Schema(description = "出库单明细ID", example = "")
    @ExcelProperty("出库单明细ID")
    private Long billItemId;

    @Schema(description = "出入方向", example = "")
    @ExcelProperty("出入方向")
    private Integer direction;

    @Schema(description = "变化的数量，可出库量的变化量", example = "")
    @ExcelProperty("变化的数量，可出库量的变化量")
    private Integer outboundAvailableDeltaQty;

    @Schema(description = "可出库量", example = "")
    @ExcelProperty("可出库量")
    private Integer outboundAvailableQty;

    @Schema(description = "可上架量", example = "")
    private Integer shelveAvailableQty;

    @Schema(description = "实际入库量", example = "")
    @ExcelProperty("实际入库量")
    private Integer actualQty;

    @Schema(description = "已上架量，已经拣货到仓位的库存量", example = "")
    @ExcelProperty("已上架量，已经拣货到仓位的库存量")
    private Integer shelveClosedQty;
}
