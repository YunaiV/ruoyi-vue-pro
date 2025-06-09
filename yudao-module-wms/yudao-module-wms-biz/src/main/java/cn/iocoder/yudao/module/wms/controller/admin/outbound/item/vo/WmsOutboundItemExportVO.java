package cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @table-fields : tenant_id,outbound_available_qty,creator,inbound_status,company_id,create_time,plan_qty,shelve_closed_qty,upstream_id,remark,latest_flow_id,updater,inbound_id,update_time,actual_qty,product_id,id,dept_id
 */
@Schema(description = "管理后台 - 入库单详情 Export VO")
@Data
@ExcelIgnoreUnannotated
public class WmsOutboundItemExportVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "27153")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库", requiredMode = Schema.RequiredMode.REQUIRED, example = "20572")
    @ExcelProperty("仓库")
    private String warehouseName;

    @Schema(description = "入库单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6602")
    @ExcelProperty("入库单号")
    private String outboundCode;

    @Schema(description = "SKU", requiredMode = Schema.RequiredMode.REQUIRED, example = "20572")
    @ExcelProperty("SKU")
    private String productCode;

    @Schema(description = "出库状态 ; WmsOutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    @ExcelProperty("出库状态")
    private String outboundStatusLabel;

    @Schema(description = "库位", example = "")
    @ExcelProperty("库位")
    private String binName;


    @Schema(description = "计划出库量", example = "")
    @ExcelProperty("计划出库量")
    private Integer planQty;

    @Schema(description = "库存财务公司ID", example = "")
    @ExcelProperty("库存财务公司ID")
    private String companyName;

    @Schema(description = "库存归属部门ID", example = "")
    @ExcelProperty("库存归属部门ID")
    private String deptName;


    @Schema(description = "备注", example = "")
    @ExcelProperty("备注")
    private String remark;










}
