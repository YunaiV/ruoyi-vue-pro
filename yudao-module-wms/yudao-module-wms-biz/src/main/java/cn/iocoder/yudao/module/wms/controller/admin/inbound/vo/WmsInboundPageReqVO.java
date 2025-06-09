package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : code,inbound_status,company_id,create_time,inbound_time,arrival_actual_time,remark,audit_status,trace_no,type,upstream_type,init_age,upstream_id,shipping_method,upstream_code,dept_id,arrival_plan_time,shelving_status,warehouse_id
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

    @Schema(description = "产品ID", example = "200")
    private Long productId;

    @Schema(description = "跟踪号")
    private String traceNo;

    @Schema(description = "WMS运输方式 ; WmsShippingMethod : 0-海运 , 1-铁路 , 2-空运 , 3-集卡")
    private Integer shippingMethod;

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
    private LocalDateTime[] arrivalActualTime;

    @Schema(description = "计划到货时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] arrivalPlanTime;

    @Schema(description = "入库时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime inboundTime;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;

    @Schema(description = "单据号", example = "")
    private String code;

    @Schema(description = "来源单据ID", example = "")
    private Long upstreamId;

    @Schema(description = "来源单据编码", example = "")
    private String upstreamCode;

    @Schema(description = "WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单 , 3-换货单", example = "")
    private Integer upstreamType;

    @Schema(description = "WMS入库单上架状态 ; WmsInboundShelvingStatus : 1-未上架 , 2-部分上架 , 3-已上架", example = "")
    private Integer shelveStatus;

    @Schema(description = "特别说明，创建方专用", example = "")
    private String remark;
}
