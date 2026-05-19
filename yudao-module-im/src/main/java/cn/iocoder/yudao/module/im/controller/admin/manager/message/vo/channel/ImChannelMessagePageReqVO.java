package cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.channel;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IM 频道消息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImChannelMessagePageReqVO extends PageParam {

    @Schema(description = "频道编号", example = "1")
    private Long channelId;

    @Schema(description = "素材编号", example = "1024")
    private Long materialId;

    @Schema(description = "发送时间", example = "[2026-04-01 00:00:00, 2026-04-30 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] sendTime;

}
