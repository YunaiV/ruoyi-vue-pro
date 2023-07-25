
package cn.iocoder.yudao.module.pay.controller.admin.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 回调通知的明细 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayNotifyTaskDetailRespVO extends PayNotifyTaskBaseVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3380")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "应用名称", example = "wx_pay")
    private String appName;

    @Schema(description = "回调日志列表")
    private List<Log> logs;

    @Schema(description = "管理后台 - 回调日志")
    @Data
    public static class Log {

        @Schema(description = "日志编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8848")
        private Long id;

        @Schema(description = "通知状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Byte status;

        @Schema(description = "当前通知次数", requiredMode = Schema.RequiredMode.REQUIRED)
        private Byte notifyTimes;

        @Schema(description = "HTTP 响应结果", requiredMode = Schema.RequiredMode.REQUIRED)
        private String response;

        @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime createTime;

    }

}
