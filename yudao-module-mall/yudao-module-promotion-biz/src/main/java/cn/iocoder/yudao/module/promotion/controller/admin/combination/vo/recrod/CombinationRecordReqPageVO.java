package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 拼团记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CombinationRecordReqPageVO extends PageParam {

    public static final Integer ALL = 0; // 全部
    public static final Integer TO_DAY = 1; // 今天
    public static final Integer YESTERDAY = 2; // 昨天
    public static final Integer LAST_SEVEN_DAYS = 3; // 最近七天
    public static final Integer LAST_30_DAYS = 4; // 最近 30 天
    public static final Integer THIS_MONTH = 5; // 本月
    public static final Integer THIS_YEAR = 6; // 本年

    @Schema(description = "日期类型", example = "0")
    private Integer dateType;

    @Schema(description = "活动状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
