package cn.iocoder.yudao.module.oa.controller.admin.task.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskStatusEnum;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaTaskPageReqVO extends PageParam {

    @Schema(description = "标题", example = "完成 OA 任务管理迁移")
    private String title;

    @Schema(description = "任务类型", example = "1")
    @InEnum(value = OaTaskTypeEnum.class, message = "任务类型必须是 {value}")
    private Integer type;

    @Schema(description = "任务状态", example = "1")
    @InEnum(value = OaTaskStatusEnum.class, message = "任务状态必须是 {value}")
    private Integer status;

    @Schema(description = "是否取消", example = "false")
    private Boolean canceled;

    @Schema(description = "发布人用户编号", example = "1")
    private Long publisherUserId;

    @Schema(description = "发布时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] publishTime;

}
