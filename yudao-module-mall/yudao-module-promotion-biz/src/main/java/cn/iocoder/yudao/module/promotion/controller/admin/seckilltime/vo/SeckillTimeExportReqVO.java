package cn.iocoder.yudao.module.promotion.controller.admin.seckilltime.vo;

import lombok.*;

import java.time.LocalTime;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 秒杀时段 Excel 导出 Request VO", description = "参数和 SeckillTimePageReqVO 是一致的")
@Data
public class SeckillTimeExportReqVO {

    @ApiModelProperty(value = "秒杀时段名称")
    private String name;

    @ApiModelProperty(value = "开始时间点")
    private LocalTime[] startTime;

    @ApiModelProperty(value = "结束时间点")
    private LocalTime[] endTime;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] createTime;

}
