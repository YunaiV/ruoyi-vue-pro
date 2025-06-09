package cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 出运跟踪信息表（由外部API更新） Response VO")
@Data
@ExcelIgnoreUnannotated
public class TmsVesselTrackingRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "上游单据类型;调拨单、头程单、退货单")
    @ExcelProperty("上游单据类型;调拨单、头程单、退货单")
    private Integer upstreamType;

    @Schema(description = "上游业务单ID，如调拨单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("上游业务单ID，如调拨单ID")
    private Long upstreamId;

    @Schema(description = "预计到港时间（ETA）")
    @ExcelProperty("预计到港时间（ETA）")
    private LocalDateTime arriveEstimateTime;

    @Schema(description = "预计离港时间（ETD）")
    @ExcelProperty("预计离港时间（ETD）")
    private LocalDateTime departEstimateTime;

    @Schema(description = "实际到港时间（ATA）")
    @ExcelProperty("实际到港时间（ATA）")
    private LocalDateTime arriveActualTime;

    @Schema(description = "实际离港时间（ATD）")
    @ExcelProperty("实际离港时间（ATD）")
    private LocalDateTime departActualTime;

    @Schema(description = "提柜时间")
    @ExcelProperty("提柜时间")
    private LocalDateTime pickupTime;

    @Schema(description = "还柜时间")
    @ExcelProperty("还柜时间")
    private LocalDateTime returnTime;

    @Schema(description = "数据来源（API渠道标识）")
    @ExcelProperty("数据来源（API渠道标识）")
    private String apiSource;

    @Schema(description = "最近同步时间")
    @ExcelProperty("最近同步时间")
    private LocalDateTime lastSyncTime;

    @Schema(description = "乐观锁")
    private Integer revision;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "中转港")
    @ExcelProperty("中转港")
    private Long transitPort;

    @Schema(description = "目的港")
    @ExcelProperty("目的港")
    private Long toPort;

    @Schema(description = "装运港")
    @ExcelProperty("装运港")
    private Long fromPort;

    @Schema(description = "船公司（供应商id）")
    @ExcelProperty("船公司（供应商id）")
    private Long carrierCompanyId;

    @Schema(description = "船公司名称")
    private String carrierCompanyName;

    @Schema(description = "船名")
    @ExcelProperty("船名")
    private String vessel;

    @Schema(description = "航次")
    @ExcelProperty("航次")
    private String voyage;

    @Schema(description = "货代公司(供应商ID)")
    @ExcelProperty("货代公司(供应商ID)")
    private Long forwarderCompanyId;

    @Schema(description = "货代公司名称")
    private String forwarderCompanyName;

    @Schema(description = "箱号")
    @ExcelProperty("箱号")
    private String containerNo;

    @Schema(description = "预计送仓时间")
    private LocalDateTime deliveryEstimateTime;

    @Schema(description = "实际送仓时间")
    private LocalDateTime deliveryActualTime;

    @Schema(description = "提单号")
    private String ladingNo;
}