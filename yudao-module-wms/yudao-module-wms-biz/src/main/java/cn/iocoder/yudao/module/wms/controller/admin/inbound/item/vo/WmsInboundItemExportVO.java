package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

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
public class WmsInboundItemExportVO {

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("仓库")
    public String warehouseName;



    @ExcelProperty("入库单")
    public String inboundCode;

    @Schema(description = "部门", example = "")
    @ExcelProperty("部门")
    private String deptName;

//    @Schema(description = "仓库ID", example = "23620")
//    @ExcelProperty("仓库ID")
//    private Long warehouseId;
//
//    @Schema(description = "仓位ID", example = "23620")
//    @ExcelProperty("仓位ID")
//    private Long binId;
//
//    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29327")
//    @ExcelProperty("入库单ID")
//    private Long inboundId;

//    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27659")
//    @ExcelProperty("标准产品ID")
//    private Long productId;

//    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
//    @ExcelProperty("创建时间")
//    private LocalDateTime createTime;

//    @Schema(description = "创建人姓名", example = "张三")
//    @ExcelProperty("创建人姓名")
//    private String creatorName;

//    @Schema(description = "更新人姓名", example = "李四")
//    @ExcelProperty("更新人姓名")
//    private String updaterName;

//    @Schema(description = "创建者", example = "")
//    @ExcelProperty("创建者")
//    private String creator;

//    @Schema(description = "租户编号", example = "")
//    @ExcelProperty("租户编号")
//    private Long tenantId;

//    @Schema(description = "更新时间", example = "")
//    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
//    @ExcelProperty("更新时间")
//    private LocalDateTime updateTime;

//    @Schema(description = "更新者", example = "")
//    @ExcelProperty("更新者")
//    private String updater;

//    @Schema(description = "产品", example = "")
//    @ExcelProperty("产品")
//    private WmsProductRespSimpleVO product;

    @ExcelProperty("SKU")
    private String productCode;

    @ExcelProperty("品名")
    private String productName;

    @Schema(description = "库龄", example = "")
    @ExcelProperty("库龄")
    private Integer age;

//    @Schema(description = "WMS入库状态 ; WmsInboundStatus : 0-未入库 , 1-部分入库 , 2-已入库", example = "")
//    @ExcelProperty("WMS入库状态")
//    private Integer inboundStatus;


    @ExcelProperty("入库状态")
    private String inboundStatusName;

//    @Schema(description = "入库单", example = "")
//    @ExcelProperty("入库单")
//    private WmsInboundSimpleRespVO inbound;


    @Schema(description = "计划入库量", example = "")
    @ExcelProperty("计划入库量")
    private Integer planQty;

    @Schema(description = "实际入库量", example = "")
    @ExcelProperty("实际入库量")
    private Integer actualQty;

    @Schema(description = "已上架量，已经拣货到仓位的库存量", example = "")
    @ExcelProperty("已上架量")
    private Integer shelveClosedQty;

    @Schema(description = "可上架量", example = "")
    private Integer shelveAvailableQty;

    @Schema(description = "批次剩余库存，出库后的剩余库存量", example = "")
    @ExcelProperty("批次剩余库存")
    private Integer outboundAvailableQty;







//    @Schema(description = "最新的流水ID", example = "")
//    @ExcelProperty("最新的流水ID")
//    private Long latestFlowId;

//    @Schema(description = "仓库", example = "")
//    private WmsWarehouseSimpleRespVO warehouse;



//    @Schema(description = "库位", example = "")
//    private WmsWarehouseBinRespVO bin;



//    @Schema(description = "库存归属部门ID", example = "")
//    @ExcelProperty("库存归属部门ID")
//    private Long deptId;

//    @Schema(description = "部门", example = "")
//    private DeptSimpleRespVO dept;



//    @Schema(description = "库存财务公司ID", example = "")
//    @ExcelProperty("库存财务公司ID")
//    private Long companyId;

    @Schema(description = "备注", example = "")
    @ExcelProperty("备注")
    private String remark;

//    @Schema(description = "财务公司", example = "")
//    private FmsCompanySimpleRespVO company;

//    @Schema(description = "来源明细行ID", example = "")
//    @ExcelProperty("来源明细行ID")
//    private Long upstreamId;








}
