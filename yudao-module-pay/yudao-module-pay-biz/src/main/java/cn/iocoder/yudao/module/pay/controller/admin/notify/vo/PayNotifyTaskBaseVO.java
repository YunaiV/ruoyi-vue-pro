package cn.iocoder.yudao.module.pay.controller.admin.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回调通知 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PayNotifyTaskBaseVO {

    @Schema(description = "应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10636")
    private Long appId;

    @Schema(description = "通知类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Byte type;

    @Schema(description = "数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6722")
    private Long dataId;

    @Schema(description = "通知状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Byte status;

    @Schema(description = "商户订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26697")
    private String merchantOrderId;

    @Schema(description = "下一次通知时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime nextNotifyTime;

    @Schema(description = "最后一次执行时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime lastExecuteTime;

    @Schema(description = "当前通知次数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Byte notifyTimes;

    @Schema(description = "最大可通知次数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Byte maxNotifyTimes;

    @Schema(description = "异步通知地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    private String notifyUrl;

}
