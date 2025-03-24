package cn.iocoder.yudao.module.wms.controller.admin.outbound.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : no,company_id,outbound_status,create_time,source_bill_no,source_bill_type,audit_status,creator_comment,dept_id,type,source_bill_id,warehouse_id
 */
@Schema(description = "管理后台 - 出库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsOutboundPageReqVO extends PageParam {

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "仓库ID", example = "16056")
    private Long warehouseId;

    @Schema(description = "出库单类型 ; OutboundType : 1-手工出库 , 2-订单出库", example = "1")
    private Integer type;

    @Schema(description = "出库单审批状态 ; OutboundAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过", example = "2")
    private Integer auditStatus;

    @Schema(description = "来源单据ID", example = "32195")
    private Long sourceBillId;

    @Schema(description = "来源单据号")
    private String sourceBillNo;

    @Schema(description = "来源单据类型 ; BillType : 0-入库单 , 1-出库单", example = "2")
    private Integer sourceBillType;

    @Schema(description = "特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "出库状态 ; OutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    private Integer outboundStatus;

    @Schema(description = "库存财务公司ID", example = "")
    private Long companyId;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;
}
