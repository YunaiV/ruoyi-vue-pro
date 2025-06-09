package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,no,creator,inbound_status,company_id,create_time,inbound_time,arrival_actual_time,audit_status,creator_comment,source_bill_id,trace_no,type,updater,update_time,init_age,shipping_method,source_bill_no,source_bill_type,id,dept_id,arrival_plan_time,warehouse_id
 */
@Schema(description = "管理后台 - 入库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundSimpleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6889")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号")
    @ExcelProperty("单据号")
    private String code;

    @Schema(description = "入库单类型 ; InboundType : 1-手工入库 , 2-采购入库", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("入库单类型")
    private Integer type;

    @Schema(description = "来源单据编码")
    @ExcelProperty("来源单据编码")
    private String sourceBillNo;

    @Schema(description = "来源单据类型 ; BillType : 0-入库单 , 1-出库单", example = "2")
    @ExcelProperty("来源单据类型")
    private Integer sourceBillType;

    @Schema(description = "跟踪号")
    @ExcelProperty("跟踪号")
    private String traceNo;

    @Schema(description = "运输方式 ; ShippingMethod : 0-海运 , 1-铁路 , 2-空运 , 3-集卡")
    @ExcelProperty("运输方式")
    private Integer shippingMethod;

    @Schema(description = "特别说明，创建方专用")
    @ExcelProperty("特别说明")
    private String creatorComment;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "仓库ID", example = "23620")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "WMS入库状态 ; WmsInboundStatus : 0-未入库 , 1-部分入库 , 2-已入库", example = "")
    @ExcelProperty("WMS入库状态")
    private Integer inboundStatus;


}
