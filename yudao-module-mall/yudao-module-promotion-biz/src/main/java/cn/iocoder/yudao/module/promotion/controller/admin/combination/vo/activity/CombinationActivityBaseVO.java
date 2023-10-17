package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 拼团活动 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author HUIHUI
 */
@Data
public class CombinationActivityBaseVO {

    @Schema(description = "拼团名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "越拼越省钱")
    @NotNull(message = "拼团名称不能为空")
    private String name;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "拼团商品不能为空")
    private Long spuId;

    @Schema(description = "总限购数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "16218")
    @NotNull(message = "总限购数量不能为空")
    private Integer totalLimitCount;

    @Schema(description = "单次限购数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "28265")
    @NotNull(message = "单次限购数量不能为空")
    private Integer singleLimitCount;

    @Schema(description = "活动时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2022-07-01 23:59:59]")
    @NotNull(message = "活动时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "活动时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2022-07-01 23:59:59]")
    @NotNull(message = "活动时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "开团人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "25222")
    @NotNull(message = "开团人数不能为空")
    private Integer userSize;

    @Schema(description = "虚拟成团", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "虚拟成团不能为空")
    private Boolean virtualGroup;

    @Schema(description = "限制时长（小时）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "限制时长不能为空")
    private Integer limitDuration;

}
