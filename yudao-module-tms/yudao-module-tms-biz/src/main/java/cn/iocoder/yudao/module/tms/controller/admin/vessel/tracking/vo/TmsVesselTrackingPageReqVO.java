package cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 出运跟踪信息表（由外部API更新）分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TmsVesselTrackingPageReqVO extends PageParam {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "上游单据类型;调拨单、头程单、退货单")
    private Integer upstreamType;

    @Schema(description = "上游业务单ID，如调拨单ID")
    private Long upstreamId;

    @Schema(description = "预计到港时间（ETA）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] arriveEstimateTime;

    @Schema(description = "预计离港时间（ETD）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] departEstimateTime;

    @Schema(description = "实际到港时间（ATA）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] arriveActualTime;

    @Schema(description = "实际离港时间（ATD）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] departActualTime;

    @Schema(description = "提柜时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] pickupTime;

    @Schema(description = "还柜时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] returnTime;

    @Schema(description = "数据来源（API渠道标识）")
    private String apiSource;

    @Schema(description = "最近同步时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lastSyncTime;

    @Schema(description = "乐观锁")
    private Integer revision;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "中转港")
    private Long transitPort;

    @Schema(description = "目的港")
    private Long toPort;

    @Schema(description = "装运港")
    private Long fromPort;

    @Schema(description = "船公司（供应商id）")
    private Long carrierCompanyId;

    @Schema(description = "船名")
    private String vessel;

    @Schema(description = "航次")
    private String voyage;

    @Schema(description = "货代公司(供应商ID)")
    private Long forwarderCompanyId;

    @Schema(description = "箱号")
    private String containerNo;

    @Schema(description = "预计送仓时间")
    private LocalDateTime deliveryEstimateTime;

    @Schema(description = "实际送仓时间")
    private LocalDateTime deliveryActualTime;

    @Schema(description = "提单号")
    private String ladingNo;
}