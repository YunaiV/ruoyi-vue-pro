package cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : bill_id,status_after,create_time,status_type,bill_type,comment,status_before
 */
@Schema(description = "管理后台 - 审批历史分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsApprovalHistoryPageReqVO extends PageParam {

    @Schema(description = "代码", example = "2")
    private String billType;

    @Schema(description = "名称", example = "29844")
    private String billId;

    @Schema(description = "状态类型", example = "1")
    private String statusType;

    @Schema(description = "审批前的状态")
    private Integer statusBefore;

    @Schema(description = "审批后状态")
    private Integer statusAfter;

    @Schema(description = "审批意见")
    private String comment;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
