package cn.iocoder.yudao.module.wms.controller.admin.outbound.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,no,creator,company_id,create_time,outbound_time,audit_status,creator_comment,type,source_bill_id,updater,update_time,latest_outbound_action_id,outbound_status,source_bill_no,source_bill_type,id,dept_id,warehouse_id
 */
@Schema(description = "管理后台 - 出库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsOutboundSimpleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "7690")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号")
    @ExcelProperty("单据号")
    private String code;

    @Schema(description = "仓库ID", example = "16056")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "出库单类型 ; OutboundType : 1-手工出库 , 2-订单出库", example = "1")
    @ExcelProperty("出库单类型")
    private Integer type;

    @Schema(description = "来源单据ID", example = "32195")
    @ExcelProperty("来源单据ID")
    private Long sourceBillId;

    @Schema(description = "来源单据编码")
    @ExcelProperty("来源单据编码")
    private String sourceBillNo;

    @Schema(description = "来源单据类型 ; BillType : 0-入库单 , 1-出库单", example = "2")
    @ExcelProperty("来源单据类型")
    private Integer sourceBillType;

    @Schema(description = "特别说明，创建方专用")
    @ExcelProperty("特别说明")
    private String creatorComment;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "出库状态 ; OutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    @ExcelProperty("出库状态")
    private Integer outboundStatus;

    @Schema(description = "出库时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("出库时间")
    private LocalDateTime outboundTime;

    @Schema(description = "计划出库时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("计划出库时间")
    private LocalDateTime outboundPlanTime;

}
