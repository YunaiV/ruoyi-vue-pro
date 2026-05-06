package cn.iocoder.yudao.module.im.controller.admin.manager.group.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IM 加群申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImGroupRequestManagerPageReqVO extends PageParam {

    @Schema(description = "群编号", example = "1024")
    private Long groupId;

    @Schema(description = "申请人 / 被邀请人用户编号", example = "2048")
    private Long userId;

    @Schema(description = "邀请人用户编号", example = "31460")
    private Long inviterUserId;

    @Schema(description = "处理结果", example = "0")
    private Integer handleResult; // 参见 ImGroupRequestHandleResultEnum 枚举类

    @Schema(description = "加入来源", example = "1")
    private Integer addSource; // 参见 ImGroupAddSourceEnum 枚举类

    @Schema(description = "创建时间", example = "[2026-04-01 00:00:00, 2026-04-30 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
