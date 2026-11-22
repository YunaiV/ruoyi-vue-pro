package cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT OPC UA Server 配置 Response VO")
@Data
public class IotOpcUaServerConfigRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号车间")
    private String name;

    @Schema(description = "凭据别名", requiredMode = Schema.RequiredMode.REQUIRED, example = "scada-reader")
    private String credentialAlias;

    @Schema(description = "最大会话数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer maxSessions;

    @Schema(description = "每个会话的最大订阅数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer maxSubscriptionsPerSession;

    @Schema(description = "每个会话的最大 MonitoredItem 数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer maxMonitoredItemsPerSession;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
