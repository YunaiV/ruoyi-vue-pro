package cn.iocoder.yudao.module.iot.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - IoT 设备上下行消息数量统计 Response VO")
@Data
public class IotStatisticsDeviceMessageSummaryRespVO {

    @Schema(description = "每小时上行数据数量统计")
    private List<Map<Long, Integer>> upstreamCounts;

    @Schema(description = "每小时下行数据数量统计")
    private List<Map<Long, Integer>> downstreamCounts;

}
