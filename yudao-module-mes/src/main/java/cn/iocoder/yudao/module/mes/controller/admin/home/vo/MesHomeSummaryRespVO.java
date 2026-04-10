package cn.iocoder.yudao.module.mes.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 首页汇总统计 Response VO")
@Data
public class MesHomeSummaryRespVO {

    // ========== 工单统计 ==========

    @Schema(description = "进行中工单数", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long workOrderActiveCount;

    @Schema(description = "待排产工单数(草稿)", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long workOrderPrepareCount;

    @Schema(description = "已完成工单数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Long workOrderFinishedCount;

    // ========== 产量统计 ==========

    @Schema(description = "今日报工总产量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234")
    private BigDecimal todayOutput;

    @Schema(description = "昨日报工总产量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1100")
    private BigDecimal yesterdayOutput;

    // ========== 质量统计 ==========

    @Schema(description = "今日合格品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1200")
    private BigDecimal todayQualifiedQuantity;

    @Schema(description = "今日不良品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "34")
    private BigDecimal todayUnqualifiedQuantity;

    // ========== 设备统计 ==========

    @Schema(description = "设备总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Long machineryTotal;

    @Schema(description = "运行中设备数", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Long machineryProducing;

    @Schema(description = "停机设备数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long machineryStop;

    @Schema(description = "维护中设备数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long machineryMaintenance;

    // ========== 异常/待办统计 ==========

    @Schema(description = "未处置安灯报警数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long andonActiveCount;

    @Schema(description = "未完成维修工单数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long repairActiveCount;

}
