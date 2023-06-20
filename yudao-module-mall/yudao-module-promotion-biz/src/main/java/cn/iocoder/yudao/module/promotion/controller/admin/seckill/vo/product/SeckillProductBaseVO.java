package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 秒杀参与商品 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author HUIHUI
 */
@Data
public class SeckillProductBaseVO {

    @Schema(description = "秒杀活动id", requiredMode = Schema.RequiredMode.REQUIRED, example = "20173")
    @NotNull(message = "秒杀活动id不能为空")
    private Long activityId;

    @Schema(description = "秒杀时段id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "秒杀时段id不能为空")
    private String configIds;

    @Schema(description = "商品spu_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "10290")
    @NotNull(message = "商品spu_id不能为空")
    private Long spuId;

    @Schema(description = "商品sku_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30563")
    @NotNull(message = "商品sku_id不能为空")
    private Long skuId;

    @Schema(description = "秒杀金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "6689")
    @NotNull(message = "秒杀金额，单位：分不能为空")
    private Integer seckillPrice;

    @Schema(description = "秒杀库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "秒杀库存不能为空")
    private Integer stock;

    @Schema(description = "秒杀商品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "秒杀商品状态不能为空")
    private Integer activityStatus;

    @Schema(description = "活动开始时间点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动开始时间点不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime activityStartTime;

    @Schema(description = "活动结束时间点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动结束时间点不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime activityEndTime;

}
