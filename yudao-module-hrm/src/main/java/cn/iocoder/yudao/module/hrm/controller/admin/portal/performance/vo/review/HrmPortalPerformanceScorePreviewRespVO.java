package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review;

import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentQuotaScoreRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工端绩效评分试算 Response VO")
@Data
public class HrmPortalPerformanceScorePreviewRespVO {

    @Schema(description = "阶段得分")
    private BigDecimal stageScore;

    @Schema(description = "阶段结果等级")
    private String stageResultLevel;

    @Schema(description = "当前累计得分")
    private BigDecimal cumulativeScore;

    @Schema(description = "当前累计结果等级；全部评分阶段完成时返回")
    private String cumulativeResultLevel;

    @Schema(description = "本阶段指标得分列表；完成率指标为服务端计算结果")
    private List<HrmPerformanceAssessmentQuotaScoreRespVO> quotaScores;

}
