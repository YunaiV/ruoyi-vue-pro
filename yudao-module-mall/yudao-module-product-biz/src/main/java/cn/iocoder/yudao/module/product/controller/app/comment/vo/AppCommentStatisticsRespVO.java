package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * APP 商品评价页评论分类数统计 Response VO
 *
 * @author HUIHUI
 */
@Schema(description = "APP - 商品评价页评论分类数统计 Response VO")
@Data
@ToString(callSuper = true)
public class AppCommentStatisticsRespVO {

    @Schema(description = "所有评论数量", required = true, example = "15721")
    private Long allCount;

    @Schema(description = "好评数量", required = true, example = "15721")
    private Long goodCount;

    @Schema(description = "中评数量", required = true, example = "15721")
    private Long mediocreCount;

    @Schema(description = "差评数量", required = true, example = "15721")
    private Long negativeCount;

}
