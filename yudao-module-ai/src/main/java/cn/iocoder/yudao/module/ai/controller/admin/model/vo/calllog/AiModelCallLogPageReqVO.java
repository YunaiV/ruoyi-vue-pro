package cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI 模型调用日志分页 Request VO")
@Data
public class AiModelCallLogPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "业务类型", example = "CHAT_MESSAGE")
    private String bizType;

    @Schema(description = "平台标识", example = "OPENAI")
    private String platform;

    @Schema(description = "模型编号", example = "1")
    private Long modelId;

    @Schema(description = "状态", example = "SUCCESS")
    private String status;

    @Schema(description = "是否被预算拦截", example = "false")
    private Boolean blocked;

    @Schema(description = "请求时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] requestTime;

}
