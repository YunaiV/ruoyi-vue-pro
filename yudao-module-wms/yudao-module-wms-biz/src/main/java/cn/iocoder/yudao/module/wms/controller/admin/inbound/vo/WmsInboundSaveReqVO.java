package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.wms.enums.common.WmsShippingMethod;
import cn.iocoder.yudao.module.wms.enums.common.WmsBillType;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import org.springframework.format.annotation.DateTimeFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : no,inbound_status,company_id,inbound_time,arrival_actual_time,audit_status,creator_comment,type,source_bill_id,trace_no,init_age,source_bill_no,shipping_method,source_bill_type,id,dept_id,warehouse_id,arrival_plan_time
 */
@Schema(description = "管理后台 - 入库单新增/修改 Request VO")
@Data
public class WmsInboundSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6889")
    @NotNull(message = "主键不能为空", groups = { ValidationGroup.update.class })
    private Long id;

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "WMS入库单类型 ; WmsInboundType : 1-手工入库 , 2-采购入库", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "WMS入库单类型不能为空", groups = { ValidationGroup.create.class })
    @InEnum(WmsInboundType.class)
    private Integer type;

    @Schema(description = "仓库ID", example = "23620")
    @NotNull(message = "仓库ID不能为空", groups = { ValidationGroup.create.class })
    private Long warehouseId;

    @Schema(description = "来源单据ID", example = "24655")
    private Long sourceBillId;

    @Schema(description = "来源单据号")
    private String sourceBillNo;

    @Schema(description = "WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单", example = "2")
    @InEnum(WmsBillType.class)
    private Integer sourceBillType;

    @Schema(description = "跟踪号")
    private String traceNo;

    @Schema(description = "WMS运输方式 ; WmsShippingMethod : 0-海运 , 1-铁路 , 2-空运 , 3-集卡")
    @InEnum(WmsShippingMethod.class)
    private Integer shippingMethod;

    @Schema(description = "特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "初始库龄")
    private Integer initAge;

    @Schema(description = "详情清单", example = "")
    private List<WmsInboundItemSaveReqVO> itemList;

    @Schema(description = "WMS入库单审批状态 ; WmsInboundAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过 , 4-强制完成 , 5-已作废", example = "")
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

    @Schema(description = "预计到货时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime arrivalPlanTime;

    @Schema(description = "入库时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime inboundTime;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;
}
