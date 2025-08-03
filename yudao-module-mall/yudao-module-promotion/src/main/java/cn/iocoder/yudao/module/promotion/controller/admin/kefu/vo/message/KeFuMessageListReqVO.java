package cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 客服消息列表 Request VO")
@Data
public class KeFuMessageListReqVO {

    private static final Integer LIMIT = 10;

    @Schema(description = "会话编号", example = "12580")
    @NotNull(message = "会话编号不能为空")
    private Long conversationId;

    @Schema(description = "发送时间", example = "2024-03-27 12:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTime;

    @Schema(description = "每次查询条数，最大值为 100", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "每次查询条数不能为空")
    @Min(value = 1, message = "每次查询条数最小值为 1")
    @Max(value = 100, message = "每次查询最大值为 100")
    private Integer limit = LIMIT;

}