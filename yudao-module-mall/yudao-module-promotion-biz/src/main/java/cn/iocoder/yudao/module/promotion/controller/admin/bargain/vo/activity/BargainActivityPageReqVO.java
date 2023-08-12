package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 砍价活动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BargainActivityPageReqVO extends PageParam {

    @Schema(description = "砍价名称", example = "赵六")
    private String name;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "活动状态：0开启 1关闭", example = "0")
    private Integer status;

}
