package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : bill_id,tenant_id,outbound_available_qty,creator,create_time,shelve_closed_qty,updater,inbound_id,outbound_available_delta_qty,outbound_action_id,update_time,actual_qty,bill_item_id,product_id,bill_type,id,inbound_item_id,direction
 */
@Schema(description = "管理后台 - 入库单库存详情扣减 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundItemFlowRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13478")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23778")
    @ExcelProperty("入库单ID")
    private Long inboundId;

    @Schema(description = "入库单明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25263")
    @ExcelProperty("入库单明细ID")
    private Long inboundItemId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30952")
    @ExcelProperty("标准产品ID")
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

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "出库动作ID", example = "")
    @ExcelProperty("出库动作ID")
    private Long outboundActionId;

    @Schema(description = "WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单 , 3-换货单", example = "")
    @ExcelProperty("WMS来源单据类型")
    private Integer billType;

    @Schema(description = "出库单ID", example = "")
    @ExcelProperty("出库单ID")
    private Long billId;

    @Schema(description = "出库单明细ID", example = "")
    @ExcelProperty("出库单明细ID")
    private Long billItemId;

    @Schema(description = "WMS库存流水方向 ; WmsStockFlowDirection : -1-流出 , 1-流入", example = "")
    @ExcelProperty("WMS库存流水方向")
    private Integer direction;

    @Schema(description = "可上架量", example = "")
    private Integer shelveAvailableQty;

    @Schema(description = "变化的数量，可出库量的变化量", example = "")
    @ExcelProperty("变化的数量")
    private Integer outboundAvailableDeltaQty;

    @Schema(description = "可出库量", example = "")
    @ExcelProperty("可出库量")
    private Integer outboundAvailableQty;

    @Schema(description = "实际入库量", example = "")
    @ExcelProperty("实际入库量")
    private Integer actualQty;

    @Schema(description = "已上架量，已经拣货到仓位的库存量", example = "")
    @ExcelProperty("已上架量，已经拣货到仓位的库存量")
    private Integer shelveClosedQty;
}
