package cn.iocoder.yudao.module.mp.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 某一天的粉丝增减数据 Response VO")
@Data
public class MpStatisticsUpstreamMessageRespVO {

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime refDate;

    @Schema(description = "上行发送了（向公众号发送了）消息的粉丝数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer messageUser;

    @Schema(description = "上行发送了消息的消息总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer messageCount;

}
