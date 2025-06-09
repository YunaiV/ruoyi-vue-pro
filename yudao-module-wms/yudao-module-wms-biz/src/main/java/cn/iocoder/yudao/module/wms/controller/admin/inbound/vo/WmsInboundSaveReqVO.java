package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.enums.common.WmsShippingMethod;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundShelvingStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : code,inbound_status,company_id,inbound_time,arrival_actual_time,remark,audit_status,trace_no,type,upstream_type,init_age,upstream_id,shipping_method,id,upstream_code,dept_id,arrival_plan_time,shelving_status,warehouse_id
 */
@Schema(description = "管理后台 - 入库单新增/修改 Request VO")
@Data
public class WmsInboundSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6889")
    @NotNull(message = "主键不能为空", groups = { ValidationGroup.update.class })
    private Long id;

    @Schema(description = "WMS入库单类型 ; WmsInboundType : 1-手工入库 , 2-采购入库 , 3-盘点入库", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "WMS入库单类型不能为空", groups = { ValidationGroup.create.class })
    @InEnum(WmsInboundType.class)
    private Integer type;

    @Schema(description = "仓库ID", example = "23620")
    @NotNull(message = "仓库ID不能为空", groups = { ValidationGroup.create.class })
    private Long warehouseId;

    @Schema(description = "跟踪号")
    private String traceNo;

    @Schema(description = "WMS运输方式 ; WmsShippingMethod : 0-海运 , 1-铁路 , 2-空运 , 3-集卡")
    @InEnum(WmsShippingMethod.class)
    private Integer shippingMethod;

    @Schema(description = "初始库龄")
    private Integer initAge;

    @Schema(description = "详情清单", example = "")
    private List<WmsInboundItemSaveReqVO> itemList;

    @Schema(description = "WMS入库单审批状态 ; WmsInboundAuditStatus : 0-草稿 , 1-待入库 , 2-驳回 , 3-已入库 , 4-强制入库 , 5-作废", example = "")
    @InEnum(WmsInboundAuditStatus.class)
    private Integer auditStatus;

    @Schema(description = "WMS入库状态 ; WmsInboundStatus : 0-未入库 , 1-部分入库 , 2-已入库", example = "")
    @InEnum(WmsInboundStatus.class)
    private Integer inboundStatus;

    @Schema(description = "库存财务公司ID", example = "")
    private Long companyId;

    @Schema(description = "实际到货时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime arrivalActualTime;

    @Schema(description = "计划到货时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime arrivalPlanTime;

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
    @InEnum(BillType.class)
    private Integer upstreamType;

    @Schema(description = "WMS入库单上架状态 ; WmsInboundShelvingStatus : 1-未上架 , 2-部分上架 , 3-已上架", example = "")
    @InEnum(WmsInboundShelvingStatus.class)
    private Integer shelveStatus;

    @Schema(description = "特别说明，创建方专用", example = "")
    private String remark;
}
