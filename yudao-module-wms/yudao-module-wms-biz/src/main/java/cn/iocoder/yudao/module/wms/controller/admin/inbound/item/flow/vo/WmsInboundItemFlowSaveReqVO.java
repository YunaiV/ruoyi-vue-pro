package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @table-fields : bill_id,outbound_available_qty,shelve_closed_qty,inbound_id,outbound_available_delta_qty,outbound_action_id,actual_qty,bill_item_id,product_id,bill_type,id,inbound_item_id,direction
 */
@Schema(description = "管理后台 - 入库单库存详情扣减新增/修改 Request VO")
@Data
public class WmsInboundItemFlowSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13478")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23778")
    @NotNull(message = "入库单ID不能为空")
    private Long inboundId;

    @Schema(description = "入库单明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25263")
    @NotNull(message = "入库单明细ID不能为空")
    private Long inboundItemId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30952")
    @NotNull(message = "标准产品ID不能为空")
    private Long productId;

    @Schema(description = "出库动作ID", example = "")
    private Long outboundActionId;

    @Schema(description = "WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单 , 3-换货单", example = "")
    @InEnum(BillType.class)
    private Integer billType;

    @Schema(description = "出库单ID", example = "")
    private Long billId;

    @Schema(description = "出库单明细ID", example = "")
    private Long billItemId;

    @Schema(description = "WMS库存流水方向 ; WmsStockFlowDirection : -1-流出 , 1-流入", example = "")
    @InEnum(WmsStockFlowDirection.class)
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
