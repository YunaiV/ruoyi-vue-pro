package cn.iocoder.yudao.module.statistics.controller.admin.trade.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.statistics.enums.TimeRangeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 交易订单量趋势统计 Request VO")
@Data
public class TradeOrderTrendReqVO {

    @Schema(description = "日期范围类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "日期范围类型不能为空")
    @InEnum(value = TimeRangeTypeEnum.class, message = "日期范围类型，必须是 {value}")
    private Integer type;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "起始时间")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "截止时间")
    private LocalDateTime endTime;

}
