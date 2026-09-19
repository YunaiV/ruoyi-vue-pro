package cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.discussion;

import cn.iocoder.yudao.framework.common.pojo.SortablePageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.discussion.OaDiscussionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 讨论分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaDiscussionPageReqVO extends SortablePageParam {

    @Schema(description = "标题", example = "产品方案讨论")
    private String title;

    @Schema(description = "讨论类型", example = "1")
    @InEnum(value = OaDiscussionTypeEnum.class, message = "讨论类型必须是 {value}")
    private Integer type;

    @Schema(description = "发布人用户编号", example = "1")
    private Long userId;

    @Schema(description = "创建时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
