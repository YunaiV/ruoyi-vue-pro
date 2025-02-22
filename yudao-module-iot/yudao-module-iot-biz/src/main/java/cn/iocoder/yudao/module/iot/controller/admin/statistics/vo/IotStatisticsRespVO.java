package cn.iocoder.yudao.module.iot.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - Iot统计 Response VO")
@Data
public class IotStatisticsRespVO {

    @Schema(description = "品类数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private long categoryTotal;

    @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private long productTotal;

    @Schema(description = "设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private long deviceTotal;

    @Schema(description = "上报数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private long reportTotal;

    @Schema(description = "今日新增品类数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private long categoryTodayTotal;

    @Schema(description = "今日新增产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private long productTodayTotal;

    @Schema(description = "今日新增设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private long deviceTodayTotal;

    @Schema(description = "今日新增上报数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private long reportTodayTotal;

    @Schema(description = "在线数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private long onlineTotal;

    @Schema(description = "离线数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private long offlineTotal;

    @Schema(description = "待激活设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private long neverOnlineTotal;

    @Schema(description = "上报数据数量统计")
    private List<TimeData> reportDataStats;

    @Schema(description = "上行数据数量统计")
    private List<TimeData> deviceUpMessageStats;

    @Schema(description = "下行数据数量统计")
    private List<TimeData> deviceDownMessageStats;

    @Schema(description = "按品类统计的设备数量")
    private List<DataItem> deviceStatsOfCategory;

    @Schema(description = "时间数据")
    @Data
    public static class TimeData {

        @Schema(description = "时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1646092800000")
        private long time;

        @Schema(description = "数据值", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Object data;
    }

    @Schema(description = "数据项")
    @Data
    public static class DataItem {

        @Schema(description = "数据项名", requiredMode = Schema.RequiredMode.REQUIRED, example = "智能家居")
        private String name;

        @Schema(description = "数据项值", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        private Object value;
    }
}
