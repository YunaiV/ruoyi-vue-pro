package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI 聊天消息的分页 Request VO")
@Data
public class AiChatMessagePageReqVO extends PageParam {

    @Schema(description = "对话编号", example = "2048")
    private Long conversationId;

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "消息内容", example = "你好")
    private String content;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
