package cn.iocoder.yudao.module.wms.controller.admin.exchange.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : code,create_time,remark,audit_status,type,warehouse_id
 */
@Schema(description = "管理后台 - 换货单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsExchangePageReqVO extends PageParam {

    @Schema(description = "单据号")
    private String code;

    @Schema(description = "WMS换货单类型 ; WmsExchangeType : 1-良品转次品 , 2-次品转良品", example = "2")
    private Integer type;

    @Schema(description = "调出仓库ID", example = "27440")
    private Long warehouseId;

    @Schema(description = "WMS换货单审批状态 ; WmsExchangeAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过", example = "2")
    private Integer auditStatus;

    @Schema(description = "特别说明", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
