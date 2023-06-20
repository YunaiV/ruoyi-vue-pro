package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

/**
 * 秒杀活动基地签证官
 * 秒杀活动 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author HUIHUI
 */
@Data
public class SeckillActivityBaseVO {

    @Schema(description = "秒杀活动商品", requiredMode = Schema.RequiredMode.REQUIRED, example = "121")
    @NotNull(message = "秒杀活动商品不能为空")
    private Long spuId;

    @Schema(description = "秒杀活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "618大促")
    @NotNull(message = "秒杀活动名称不能为空")
    private String name;

    @Schema(description = "活动状态 开启：0 禁用：1", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "活动状态 开启：0 禁用：1不能为空")
    private Integer status;

    @Schema(description = "备注", example = "清仓大甩卖割韭菜")
    private String remark;

    @Schema(description = "活动开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "秒杀时段id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "秒杀时段id不能为空")
    private List<Long> configIds;

    @Schema(description = "总限购数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12877")
    private Integer totalLimitCount;

    @Schema(description = "单次限够数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "31683")
    private Integer singleLimitCount;

    @Schema(description = "秒杀总库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer totalStock;

}
