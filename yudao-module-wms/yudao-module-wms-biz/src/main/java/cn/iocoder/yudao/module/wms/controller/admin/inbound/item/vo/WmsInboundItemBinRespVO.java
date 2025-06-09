package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @table-fields : tenant_id,outbound_available_qty,creator,inbound_status,company_id,create_time,plan_qty,shelve_closed_qty,upstream_id,remark,inbound_dept_id,latest_flow_id,updater,inbound_id,inbound_company_id,update_time,actual_qty,product_id,id,dept_id
 */
@Schema(description = "管理后台 - 入库单详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundItemBinRespVO extends WmsInboundItemRespVO {


    private Integer lineNumber;
    /**
     * 仓位ID
     */
    @Schema(description = "仓位ID", example = "")
    private Long binId;

    /**
     * 仓位名称
     */
    @Schema(description = "仓位名称", example = "")
    private String binName;

    /**
     * 库存类型
     */
    @Schema(description = "库存类型", example = "")
    private Integer stockType;

    /**
     * 仓位可用库存
     */
    @Schema(description = "仓位可用库存", example = "")
    private Integer binAvailableQty;
    /**
     * 仓位可售库存
     */
    @Schema(description = "仓位可售库存", example = "")
    private Integer binSellableQty;
    /**
     * 仓位待出库库存
     */
    @Schema(description = "仓位待出库库存", example = "")
    private Integer binOutboundPendingQty;
    /**
     * 上架单ID
     */
    @Schema(description = "待上单ID", example = "")
    private String pickupId;
    /**
     * 上架数量
     */
    @Schema(description = "上架数量", example = "")
    private Integer pickupQty;
    /**
     * 上架单号
     */
    @Schema(description = "上架单号", example = "")
    private String pickupCode;

    /**
     * 入库单号
     */
    @Schema(description = "入库单号", example = "")
    private String inboundCode;


}
