package cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import lombok.Data;

@Schema(description = "管理后台 - IoT OPC UA Server 配置新增/修改 Request VO")
@Data
public class IotOpcUaServerConfigSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号车间")
    @NotEmpty(message = "配置名称不能为空")
    @Size(max = 64, message = "配置名称长度不能超过 64 个字符")
    private String name;

    @Schema(description = "凭据别名（对应网关本地配置的 credentials）", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "scada-reader")
    @NotEmpty(message = "凭据别名不能为空")
    @Size(max = 64, message = "凭据别名长度不能超过 64 个字符")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "凭据别名只能包含字母、数字、下划线、点和中划线")
    private String credentialAlias;

    @Schema(description = "最大会话数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "最大会话数不能为空")
    @Min(value = 1, message = "最大会话数不能小于 1")
    @Max(value = 100, message = "最大会话数不能大于 100")
    private Integer maxSessions;

    @Schema(description = "每个会话的最大订阅数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "每个会话的最大订阅数不能为空")
    @Min(value = 1, message = "每个会话的最大订阅数不能小于 1")
    @Max(value = 100, message = "每个会话的最大订阅数不能大于 100")
    private Integer maxSubscriptionsPerSession;

    @Schema(description = "每个会话的最大 MonitoredItem 数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "每个会话的最大 MonitoredItem 数不能为空")
    @Min(value = 1, message = "每个会话的最大 MonitoredItem 数不能小于 1")
    @Max(value = 10000, message = "每个会话的最大 MonitoredItem 数不能大于 10000")
    private Integer maxMonitoredItemsPerSession;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
