package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 秒杀活动 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SeckillActivityBaseVO {

    @Schema(description = "秒杀活动名称", required = true, example = "晚九点限时秒杀")
    @NotNull(message = "秒杀活动名称不能为空")
    private String name;

    @Schema(description = "活动开始时间", required = true)
    @NotNull(message = "活动开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", required = true)
    @NotNull(message = "活动结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime endTime;


    @Schema(description = "商品")
    @Data
    public static class Product {

        @Schema(description = "商品 SPU 编号", required = true, example = "1")
        @NotNull(message = "商品 SPU 编号不能为空")
        private Long spuId;

        @Schema(description = "商品 SKU 编号", required = true, example = "1")
        @NotNull(message = "商品 SKU 编号不能为空")
        private Long skuId;

        @Schema(description = "秒杀金额", required = true, example = "12.00")
        @NotNull(message = "秒杀金额不能为空")
        private Integer seckillPrice;

        @Schema(description = "秒杀库存", example = "80")
        @Min(value = 0, message = "秒杀库存需要大于等于 0")
        private Integer stock;

        @Schema(description = "每人限购", example = "10") // 如果为 0 则不限购
        @Min(value = 0, message = "每人限购需要大于等于 0")
        private Integer limitBuyCount;

    }

}
