package cn.iocoder.yudao.module.iot.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

// TODO @super：Total 全部改成 Count
// TODO @super：IotStatisticsSummaryRespVO
/**
 * 管理后台 - Iot 统计 Response VO
 */
@Schema(description = "管理后台 - Iot 统计 Response VO")
@Data
public class IotStatisticsSummaryRespVO {

    // TODO @super：productCategory 哈
    @Schema(description = "品类数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private long productCategoryCount;

    @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private long productCount;

    @Schema(description = "设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private long deviceCount;

    // TODO @super：deviceMessageCount；设备消息数量
    @Schema(description = "上报数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private long deviceMessageCount;

    // TODO @super：productCategory 哈
    @Schema(description = "今日新增品类数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private long productCategoryTodayCount;

    @Schema(description = "今日新增产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private long productTodayCount;

    @Schema(description = "今日新增设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private long deviceTodayCount;

    // TODO @super：deviceMessageCount；今日设备消息数量
    @Schema(description = "今日新增上报数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private long deviceMessageTodayCount;

    // TODO @super：deviceOnlineCount

    @Schema(description = "在线数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private long deviceOnlineCount;

    // TODO @super：deviceOfflineCount

    @Schema(description = "离线数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private long deviceOfflineCount;

    // TODO @super：deviceInactivECount

    @Schema(description = "待激活设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private long deviceInactiveCount;

    // TODO @super：1）类型改成 Map，key 分类名、value 设备数量；2）deviceStatsOfCategory => productCategoryDeviceCounts
    @Schema(description = "按品类统计的设备数量")
    private Map<String, Integer> productCategoryDeviceCounts;

    // TODO @super：貌似界面里，用不到这个字段？？？


    // TODO @super：deviceUpMessageStats、deviceDownMessageStats 单独抽到 IotStatisticsDeviceMessageSummaryRespVO，然后里面属性就是 upstreamCounts、downstreamCounts

}
