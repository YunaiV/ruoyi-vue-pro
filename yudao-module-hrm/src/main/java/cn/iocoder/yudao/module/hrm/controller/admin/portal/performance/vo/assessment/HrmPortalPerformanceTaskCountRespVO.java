package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - HRM 员工端绩效任务数量 Response VO")
@Data
@Accessors(chain = true)
public class HrmPortalPerformanceTaskCountRespVO {

    @Schema(description = "待填写指标数量")
    private Long fillPendingCount;

    @Schema(description = "已填写指标数量")
    private Long fillCompletedCount;

    @Schema(description = "待确认目标数量")
    private Long targetPendingCount;

    @Schema(description = "已确认目标数量")
    private Long targetCompletedCount;

    @Schema(description = "待评分数量")
    private Long reviewPendingCount;

    @Schema(description = "已评分数量")
    private Long reviewCompletedCount;

    @Schema(description = "待审核结果数量")
    private Long resultAuditPendingCount;

    @Schema(description = "已审核结果数量")
    private Long resultAuditCompletedCount;

    @Schema(description = "待确认结果数量")
    private Long resultConfirmationPendingCount;

    @Schema(description = "已确认结果数量")
    private Long resultConfirmationCompletedCount;

    @Schema(description = "已申诉结果数量")
    private Long resultConfirmationAppealedCount;

    @Schema(description = "待处理申诉数量")
    private Long appealPendingCount;

    @Schema(description = "已处理申诉数量")
    private Long appealCompletedCount;

}
