package cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : code,create_time,remark,audit_status,warehouse_id
 */
@Schema(description = "管理后台 - 盘点分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockCheckPageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "26854")
    private Long warehouseId;

    @Schema(description = "WMS盘点单审批状态 ; WmsStockCheckAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过 , 5-作废", example = "2")
    private Integer auditStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "单据号", example = "")
    private String code;

    @Schema(description = "创建者备注", example = "")
    private String remark;
}
