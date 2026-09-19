package cn.iocoder.yudao.module.oa.controller.admin.note.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.note.OaNoteTypeEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 笔记分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaNotePageReqVO extends PageParam {

    @Schema(description = "标题", example = "会议纪要")
    private String title;

    @Schema(description = "目录编号", example = "1024")
    private Long categoryId;

    @Schema(description = "笔记类型", example = "1")
    @InEnum(value = OaNoteTypeEnum.class, message = "笔记类型必须是 {value}")
    private Integer type;

    @Schema(description = "优先级", example = "1")
    @InEnum(value = OaPriorityEnum.class, message = "优先级必须是 {value}")
    private Integer priority;

    @Schema(description = "是否收藏", example = "false")
    private Boolean favorite;

    @Schema(description = "创建时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;


}
