package cn.iocoder.yudao.module.wms.controller.admin.outbound.vo;

import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemSaveReqVO;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundType;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.common.WmsBillType;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : code,company_id,remark,outbound_time,audit_status,creator_comment,type,upstream_bill_type,latest_outbound_action_id,outbound_status,upstream_bill_id,id,upstream_bill_code,dept_id,warehouse_id
 */
@Schema(description = "管理后台 - 出库单新增/修改 Request VO")
@Data
public class WmsOutboundSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "7690")
    @NotNull(message = "主键不能为空", groups = { ValidationGroup.update.class })
    private Long id;

    @Schema(description = "仓库ID", example = "16056")
    @NotNull(message = "仓库ID不能为空", groups = { ValidationGroup.create.class })
    private Long warehouseId;

    @Schema(description = "WMS出库单类型 ; WmsOutboundType : 1-手工出库 , 2-订单出库 , 3-盘点出库", example = "1")
    @NotNull(message = "WMS出库单类型不能为空", groups = { ValidationGroup.create.class })
    @InEnum(WmsOutboundType.class)
    private Integer type;

    @Schema(description = "WMS出库单审批状态 ; WmsOutboundAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过 , 4-已出库", example = "2")
    @InEnum(WmsOutboundAuditStatus.class)
    private Integer auditStatus;

    @Schema(description = "特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "详情清单", example = "")
    private List<WmsOutboundItemSaveReqVO> itemList;

    @Schema(description = "WMS出库状态 ; WmsOutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    @InEnum(WmsOutboundStatus.class)
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

    @Schema(description = "备注", example = "")
    private String remark;

    @Schema(description = "单据号", example = "")
    private String code;

    @Schema(description = "来源单据ID", example = "")
    private Long upstreamBillId;

    @Schema(description = "来源单据号", example = "")
    private String upstreamBillCode;

    @Schema(description = "WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单", example = "")
    private Integer upstreamBillType;
}
