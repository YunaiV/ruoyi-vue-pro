package cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.group;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IM 群聊消息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImGroupMessageManagerPageReqVO extends PageParam {

    @Schema(description = "群编号", example = "1024")
    private Long groupId;

    @Schema(description = "发送人编号", example = "1024")
    private Long senderId;

    // TODO @AI：参见 ImMessageTypeEnum 枚举类，都放到 // 注释后面。注意加到我的 memory 里。其它类似也处理下。im 的。
    @Schema(description = "消息类型，参见 ImMessageTypeEnum 枚举类", example = "1")
    private Integer type;

    @Schema(description = "消息状态，参见 ImMessageStatusEnum 枚举类", example = "0")
    private Integer status;

    @Schema(description = "发送时间", example = "[2026-04-01 00:00:00, 2026-04-30 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] sendTime;

}
