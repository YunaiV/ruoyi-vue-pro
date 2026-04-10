package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 砍价活动 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author HUIHUI
 */
@Data
public class BargainActivityBaseVO {

    @Schema(description = "砍价活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "砍得越多省得越多，是兄弟就来砍我")
    @NotNull(message = "砍价名称不能为空")
    private String name;

    @Schema(description = "商品 SPU 编号", example = "1")
    @NotNull(message = "砍价商品不能为空")
    private Long spuId;

    @Schema(description = "商品 skuId", requiredMode = Schema.RequiredMode.REQUIRED, example = "23")
    @NotNull(message = "商品 skuId 不能为空")
    private Long skuId;

    @Schema(description = "砍价起始价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "23")
    @NotNull(message = "砍价起始价格不能为空")
    private Integer bargainFirstPrice;

    @Schema(description = "砍价底价", requiredMode = Schema.RequiredMode.REQUIRED, example = "23")
    @NotNull(message = "砍价底价不能为空")
    private Integer bargainMinPrice;

    @Schema(description = "活动库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "23")
    @NotNull(message = "活动库存不能为空")
    private Integer stock;

    @Schema(description = "总限购数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "16218")
    @NotNull(message = "总限购数量不能为空")
    private Integer totalLimitCount;

    @Schema(description = "活动开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2022-07-01 23:59:59]")
    @NotNull(message = "活动开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2022-07-01 23:59:59]")
    @NotNull(message = "活动结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "最大助力次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "25222")
    @NotNull(message = "最大助力次数不能为空")
    private Integer helpMaxCount;

    @Schema(description = "最大帮砍次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "25222")
    @NotNull(message = "最大帮砍次数不能为空")
    private Integer bargainCount;

    @Schema(description = "用户每次砍价的最小金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "25222")
    @NotNull(message = "用户每次砍价的最小金额不能为空")
    private Integer randomMinPrice;

    @Schema(description = "用户每次砍价的最大金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "25222")
    @NotNull(message = "用户每次砍价的最大金额不能为空")
    private Integer randomMaxPrice;

}
