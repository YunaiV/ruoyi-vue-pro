package cn.iocoder.yudao.module.pay.controller.admin.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 回调通知 Response VO")
@Data
public class PayNotifyTaskRespVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3380")
    private Long id;

    @Schema(description = "应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10636")
    private Long appId;

    @Schema(description = "应用名称", example = "wx_pay")
    private String  appName;

    @Schema(description = "通知类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Byte type;

    @Schema(description = "数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6722")
    private Long dataId;

    @Schema(description = "商户订单编号", example = "26697")
    private String merchantOrderId;

    @Schema(description = "商户退款编号", example = "26697")
    private String merchantRefundId;

    @Schema(description = "商户转账编号", example = "26697")
    private String merchantTransferId;

    @Schema(description = "通知状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Byte status;

    @Schema(description = "下一次通知时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime nextNotifyTime;

    @Schema(description = "最后一次执行时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime lastExecuteTime;

    @Schema(description = "当前通知次数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Byte notifyTimes;

    @Schema(description = "最大可通知次数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Byte maxNotifyTimes;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
