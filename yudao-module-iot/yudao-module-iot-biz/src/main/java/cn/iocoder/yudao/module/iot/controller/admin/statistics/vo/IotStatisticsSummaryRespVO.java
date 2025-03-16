package cn.iocoder.yudao.module.iot.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 管理后台 - IoT 统计 Response VO
 */
@Schema(description = "管理后台 - IoT 统计 Response VO")
@Data
public class IotStatisticsSummaryRespVO {

    @Schema(description = "品类数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long productCategoryCount;

    @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Long productCount;

    @Schema(description = "设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long deviceCount;

    @Schema(description = "上报数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Long deviceMessageCount;

    @Schema(description = "今日新增品类数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long productCategoryTodayCount;

    @Schema(description = "今日新增产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Long productTodayCount;

    @Schema(description = "今日新增设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long deviceTodayCount;

    @Schema(description = "今日新增上报数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Long deviceMessageTodayCount;

    @Schema(description = "在线数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Long deviceOnlineCount;

    @Schema(description = "离线数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Long deviceOfflineCount;

    @Schema(description = "待激活设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long deviceInactiveCount;

    @Schema(description = "按品类统计的设备数量")
    private Map<String, Integer> productCategoryDeviceCounts;

}
