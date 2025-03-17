package cn.iocoder.yudao.module.wms.controller.admin.outbound.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 出库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsOutboundPageReqVO extends PageParam {

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "仓库ID", example = "16056")
    private Long warehouseId;

    @Schema(description = "类型", example = "1")
    private Integer type;

    @Schema(description = "状态", example = "1")
    private String status;

    @Schema(description = "审核状态", example = "2")
    private Integer auditStatus;

    @Schema(description = "来源单据ID", example = "32195")
    private Long sourceBillId;

    @Schema(description = "来源单据号")
    private String sourceBillNo;

    @Schema(description = "来源单据类型", example = "2")
    private Integer sourceBillType;

    @Schema(description = "特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}