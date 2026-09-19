package cn.iocoder.yudao.module.oa.controller.admin.announcement.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.announcement.OaAnnouncementTypeEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 公告分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaAnnouncementPageReqVO extends PageParam {

    @Schema(description = "公告标题，模糊匹配", example = "国庆节放假通知")
    private String title;

    @Schema(description = "公告类型", example = "1")
    @InEnum(value = OaAnnouncementTypeEnum.class, message = "公告类型必须是 {value}")
    private Integer type;

    @Schema(description = "优先级", example = "1")
    @InEnum(value = OaPriorityEnum.class, message = "优先级必须是 {value}")
    private Integer priority;

    @Schema(description = "是否已读", example = "false")
    private Boolean readStatus;

    @Schema(description = "发布时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
