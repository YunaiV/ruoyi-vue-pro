package cn.iocoder.yudao.module.wms.controller.admin.outbound.vo;

import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.util.List;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemSaveReqVO;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.outbound.OutboundType;
import cn.iocoder.yudao.module.wms.enums.outbound.OutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.common.BillType;
import cn.iocoder.yudao.module.wms.enums.outbound.OutboundStatus;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : no,company_id,outbound_time,audit_status,creator_comment,type,source_bill_id,latest_outbound_action_id,outbound_status,source_bill_no,source_bill_type,id,dept_id,warehouse_id
 */
@Schema(description = "管理后台 - 出库单新增/修改 Request VO")
@Data
public class WmsOutboundSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "7690")
    @NotNull(message = "主键不能为空", groups = { ValidationGroup.update.class })
    private Long id;

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "仓库ID", example = "16056")
    @NotNull(message = "仓库ID不能为空", groups = { ValidationGroup.create.class })
    private Long warehouseId;

    @Schema(description = "出库单类型 ; OutboundType : 1-手工出库 , 2-订单出库", example = "1")
    @NotNull(message = "出库单类型不能为空", groups = { ValidationGroup.create.class })
    @InEnum(OutboundType.class)
    private Integer type;

    @Schema(description = "出库单审批状态 ; OutboundAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过", example = "2")
    @InEnum(OutboundAuditStatus.class)
    private Integer auditStatus;

    @Schema(description = "来源单据ID", example = "32195")
    private Long sourceBillId;

    @Schema(description = "来源单据号")
    private String sourceBillNo;

    @Schema(description = "来源单据类型 ; BillType : 0-入库单 , 1-出库单", example = "2")
    @InEnum(BillType.class)
    private Integer sourceBillType;

    @Schema(description = "特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "详情清单", example = "")
    private List<WmsOutboundItemSaveReqVO> itemList;

    @Schema(description = "出库状态 ; OutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    @InEnum(OutboundStatus.class)
    private Integer outboundStatus;

    @Schema(description = "库存财务公司ID", example = "")
    private Long companyId;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;

    @Schema(description = "出库时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime outboundTime;

    @Schema(description = "出库动作ID，与flow关联", example = "")
    private Long latestOutboundActionId;
}
