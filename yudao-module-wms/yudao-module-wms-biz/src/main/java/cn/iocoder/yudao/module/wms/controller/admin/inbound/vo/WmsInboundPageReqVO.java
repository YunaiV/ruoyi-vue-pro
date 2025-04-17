package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : code,inbound_status,company_id,inbound_time,create_time,arrival_actual_time,audit_status,creator_comment,type,source_bill_id,trace_no,source_bill_code,init_age,shipping_method,source_bill_type,dept_id,warehouse_id,arrival_plan_time
 */
@Schema(description = "管理后台 - 入库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInboundPageReqVO extends PageParam {

    @Schema(description = "WMS入库单类型 ; WmsInboundType : 1-手工入库 , 2-采购入库 , 3-盘点入库", example = "1")
    private Integer type;

    @Schema(description = "仓库ID", example = "23620")
    private Long warehouseId;

    @Schema(description = "来源单据ID", example = "24655")
    private Long sourceBillId;

    @Schema(description = "WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单", example = "2")
    private Integer sourceBillType;

    @Schema(description = "跟踪号")
    private String traceNo;

    @Schema(description = "WMS运输方式 ; WmsShippingMethod : 0-海运 , 1-铁路 , 2-空运 , 3-集卡")
    private Integer shippingMethod;

    @Schema(description = "特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "初始库龄")
    private Integer initAge;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "WMS入库单审批状态 ; WmsInboundAuditStatus : 0-草稿 , 1-待入库 , 2-驳回 , 3-已入库 , 4-强制入库 , 5-作废", example = "")
    private Integer auditStatus;

    @Schema(description = "WMS入库状态 ; WmsInboundStatus : 0-未入库 , 1-部分入库 , 2-已入库", example = "")
    private Integer inboundStatus;

    @Schema(description = "库存财务公司ID", example = "")
    private Long companyId;

    @Schema(description = "实际到货时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime arrivalActualTime;

    @Schema(description = "预计到货时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime arrivalPlanTime;

    @Schema(description = "入库时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime inboundTime;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;

    @Schema(description = "单据号", example = "")
    private String code;

    @Schema(description = "来源单据号", example = "")
    private String sourceBillCode;
}
