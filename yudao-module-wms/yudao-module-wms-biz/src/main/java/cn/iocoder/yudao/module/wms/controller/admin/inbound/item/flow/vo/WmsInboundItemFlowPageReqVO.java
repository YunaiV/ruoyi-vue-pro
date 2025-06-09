package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : bill_id,outbound_available_qty,create_time,shelve_closed_qty,inbound_id,outbound_available_delta_qty,outbound_action_id,actual_qty,bill_item_id,product_id,bill_type,inbound_item_id,direction
 */
@Schema(description = "管理后台 - 入库单库存详情扣减分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInboundItemFlowPageReqVO extends PageParam {

    @Schema(description = "入库单ID", example = "23778")
    private Long inboundId;

    @Schema(description = "入库单明细ID", example = "25263")
    private Long inboundItemId;

    @Schema(description = "标准产品ID", example = "30952")
    private Long productId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "出库动作ID", example = "")
    private Long outboundActionId;

    @Schema(description = "WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单 , 3-换货单", example = "")
    private Integer billType;

    @Schema(description = "出库单ID", example = "")
    private Long billId;

    @Schema(description = "出库单明细ID", example = "")
    private Long billItemId;

    @Schema(description = "WMS库存流水方向 ; WmsStockFlowDirection : -1-流出 , 1-流入", example = "")
    private Integer direction;

    @Schema(description = "变化的数量，可出库量的变化量", example = "")
    private Integer outboundAvailableDeltaQty;

    @Schema(description = "可出库量", example = "")
    private Integer outboundAvailableQty;

    @Schema(description = "实际入库量", example = "")
    private Integer actualQty;

    @Schema(description = "已上架量，已经拣货到仓位的库存量", example = "")
    private Integer shelveClosedQty;
}
