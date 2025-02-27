package cn.iocoder.yudao.module.iot.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

// TODO @super：Total 全部改成 Count
// TODO @super：IotStatisticsSummaryRespVO
/**
 * 管理后台 - Iot 统计 Response VO
 */
@Schema(description = "管理后台 - Iot 统计 Response VO")
@Data
public class IotStatisticsRespVO {

    // TODO @super：productCategory 哈
    @Schema(description = "品类数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private long categoryTotal;

    @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private long productTotal;

    @Schema(description = "设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private long deviceTotal;

    // TODO @super：deviceMessageCount；设备消息数量
    @Schema(description = "上报数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private long reportTotal;

    // TODO @super：productCategory 哈
    @Schema(description = "今日新增品类数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private long categoryTodayTotal;

    @Schema(description = "今日新增产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private long productTodayTotal;

    @Schema(description = "今日新增设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private long deviceTodayTotal;

    // TODO @super：deviceMessageCount；今日设备消息数量
    @Schema(description = "今日新增上报数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private long reportTodayTotal;

    // TODO @super：deviceOnlineCount

    @Schema(description = "在线数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private long onlineTotal;

    // TODO @super：deviceOfflineCount

    @Schema(description = "离线数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private long offlineTotal;

    // TODO @super：deviceInactivECount

    @Schema(description = "待激活设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private long neverOnlineTotal;

    // TODO @super：1）类型改成 Map，key 分类名、value 设备数量；2）deviceStatsOfCategory => productCategoryDeviceCounts
    @Schema(description = "按品类统计的设备数量")
    private List<DataItem> deviceStatsOfCategory;

    // TODO @super：貌似界面里，用不到这个字段？？？
    @Schema(description = "上报数据数量统计")
    private List<TimeData> reportDataStats;

    // TODO @super：deviceUpMessageStats、deviceDownMessageStats 单独抽到 IotStatisticsDeviceMessageSummaryRespVO，然后里面属性就是 upstreamCounts、downstreamCounts

    @Schema(description = "上行数据数量统计")
    private List<TimeData> deviceUpMessageStats;

    @Schema(description = "下行数据数量统计")
    private List<TimeData> deviceDownMessageStats;

    // TODO @super：如果只有这两个字段，使用 KeyValue 这个键值对
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
