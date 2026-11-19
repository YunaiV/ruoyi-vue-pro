package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTaskTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI 短剧生成任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AiDramaTaskPageReqVO extends PageParam {

    @Schema(description = "任务类型", example = "4")
    @InEnum(AiDramaTaskTypeEnum.class)
    private Integer type;

    @Schema(description = "状态", example = "50")
    private Integer status;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "模型编号", example = "1")
    private Long modelId;

    @Schema(description = "更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] updateTime;

}
