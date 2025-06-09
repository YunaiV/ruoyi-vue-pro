package cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 出运跟踪信息表（由外部API更新）新增/修改 Request VO")
@Data
public class TmsVesselTrackingSaveReqVO {
    @Schema(description = "id")
    @NotNull(groups = {Validation.OnUpdate.class}, message = "更新时，出运跟踪信息id不能为空")
    @Null(groups = {Validation.OnCreate.class}, message = "创建时，出运跟踪信息id需为空")
    private Long id;

    @Schema(description = "上游单据类型;调拨单、头程单、退货单")
    private Integer upstreamType;

    @Schema(description = "上游业务单ID，如调拨单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "出运跟踪信息上游单据ID不能为空")
    private Long upstreamId;

    @Schema(description = "预计到港时间（ETA）")
    private LocalDateTime arriveEstimateTime;

    @Schema(description = "预计离港时间（ETD）")
    private LocalDateTime departEstimateTime;

    @Schema(description = "实际到港时间（ATA）")
    private LocalDateTime arriveActualTime;

    @Schema(description = "实际离港时间（ATD）")
    private LocalDateTime departActualTime;

    @Schema(description = "提柜时间")
    private LocalDateTime pickupTime;

    @Schema(description = "还柜时间")
    private LocalDateTime returnTime;

    @Schema(description = "数据来源（API渠道标识）")
    private String apiSource;

    @Schema(description = "中转港")
    private Long transitPort;

    @Schema(description = "目的港")
    @NotNull(message = "目的港不能为空")
    private Long toPort;

    @Schema(description = "装运港")
    @NotNull(message = "装运港不能为空")
    @Min(value = 1, message = "装运港不能小于1")
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

    @Schema(description = "乐观锁")
    private Integer revision;

    @Schema(description = "预计送仓时间")
    private LocalDateTime deliveryEstimateTime;

    @Schema(description = "实际送仓时间")
    private LocalDateTime deliveryActualTime;

    @Schema(description = "提单号")
    private String ladingNo;
}