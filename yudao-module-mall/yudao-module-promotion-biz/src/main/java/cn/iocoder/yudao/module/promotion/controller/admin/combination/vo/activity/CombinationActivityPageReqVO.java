package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 拼团活动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CombinationActivityPageReqVO extends PageParam {

    @Schema(description = "拼团名称", example = "赵六")
    private String name;

    @Schema(description = "商品 SPU 编号关联 ProductSpuDO 的 id", example = "14016")
    private Long spuId;

    @Schema(description = "总限购数量", example = "16218")
    private Integer totalLimitCount;

    @Schema(description = "单次限购数量", example = "28265")
    private Integer singleLimitCount;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "开团人数")
    private Integer userSize;

    @Schema(description = "开团组数")
    private Integer totalNum;

    @Schema(description = "成团组数")
    private Integer successNum;

    @Schema(description = "参与人数", example = "25222")
    private Integer orderUserCount;

    @Schema(description = "虚拟成团")
    private Integer virtualGroup;

    @Schema(description = "活动状态：0开启 1关闭", example = "0")
    private Integer status;

    @Schema(description = "限制时长（小时）")
    private Integer limitDuration;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
