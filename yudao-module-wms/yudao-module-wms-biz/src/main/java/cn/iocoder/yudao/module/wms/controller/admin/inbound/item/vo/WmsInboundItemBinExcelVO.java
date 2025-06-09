package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @table-fields : tenant_id,outbound_available_qty,creator,inbound_status,company_id,create_time,plan_qty,shelve_closed_qty,upstream_id,remark,inbound_dept_id,latest_flow_id,updater,inbound_id,inbound_company_id,update_time,actual_qty,product_id,id,dept_id
 */
@Schema(description = "管理后台 - 入库单详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundItemBinExcelVO {

    private Integer lineNumber;

    /**
     * 入库明细ID
     */
    @ExcelIgnore
    private Long id;

    /**
     * 仓位ID
     */
    @ExcelIgnore
    private Long binId;

    @ExcelProperty("公司")
    private String addressLine3;

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("仓库")
    private String warehouseName;

    /**
     * 仓位名称
     */
    @ExcelProperty("仓位")
    private String binName;

    @ExcelProperty("SKU")
    private String productCode;

    @ExcelProperty("库龄")
    private Integer age;

    /**
     * 库存类型
     */
    @Schema(description = "库存类型", example = "")
    private String stockTypeLabel;

    @ExcelProperty("计划入库量")
    private Integer planQty;

    @ExcelProperty("实际入库量")
    private Integer actualQty;

    @ExcelProperty("已上架量")
    private Integer shelveClosedQty;

    @Schema(description = "可上架量", example = "")
    private Integer shelveAvailableQty;

    @ExcelProperty("批次剩余库存")
    private Integer outboundAvailableQty;

    /**
     * 仓位可用库存
     */
    @ExcelProperty("仓位可用库存")
    private Integer binAvailableQty;

    /**
     * 仓位可售库存
     */
    @ExcelProperty("仓位可用库存")
    private Integer binSellableQty;

    /**
     * 仓位待出库库存
     */
    @ExcelProperty("仓位可用库存")
    private Integer binOutboundPendingQty;

    /**
     * 上架数量
     */
    @ExcelProperty("上架数量")
    private Integer pickupQty;

    /**
     * 上架单号
     */
    @ExcelProperty("上架单号")
    private String pickupCode;

    /**
     * 入库单号
     */
    @ExcelProperty("入库单号")
    private String inboundCode;




}
