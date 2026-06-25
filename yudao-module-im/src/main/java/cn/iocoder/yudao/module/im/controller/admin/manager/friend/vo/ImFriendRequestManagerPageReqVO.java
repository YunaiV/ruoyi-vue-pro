package cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IM 好友申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImFriendRequestManagerPageReqVO extends PageParam {

    @Schema(description = "发起方用户编号", example = "1024")
    private Long fromUserId;

    @Schema(description = "接收方用户编号", example = "2048")
    private Long toUserId;

    @Schema(description = "处理结果", example = "0")
    private Integer handleResult; // 参见 ImFriendRequestHandleResultEnum 枚举类

    @Schema(description = "添加来源", example = "1")
    private Integer addSource; // 参见 ImFriendAddSourceEnum 枚举类

    @Schema(description = "创建时间", example = "[2026-04-01 00:00:00, 2026-04-30 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
