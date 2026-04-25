package cn.iocoder.yudao.module.product.controller.app.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "用户 App - 可信评价提交 Response VO")
@Data
public class AppTrustworthyReviewRespVO {

    @Schema(description = "处理状态：success / rejected")
    private String status;

    @Schema(description = "拒绝原因（status=rejected 时）")
    private String reason;

    @Schema(description = "生成的评论 ID")
    private Long reviewId;

    @Schema(description = "可信度分数（0-1）")
    private Double trustScore;

    @Schema(description = "加权评分（综合可信度后的最终星级）")
    private Double weightedRating;

    @Schema(description = "是否展示给其他用户")
    private Boolean visible;

    @Schema(description = "区块链存证任务 ID")
    private String blockchainTaskId;

    @Schema(description = "各维度可信度因子")
    private Map<String, Double> trustFactors;

}
