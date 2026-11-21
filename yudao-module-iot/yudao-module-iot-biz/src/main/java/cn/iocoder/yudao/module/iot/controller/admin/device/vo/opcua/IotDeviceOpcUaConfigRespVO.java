package cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 设备 OPC UA 连接配置 Response VO")
@Data
public class IotDeviceOpcUaConfigRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceId;

    @Schema(description = "端点地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "opc.tcp://192.168.1.10:4840")
    private String endpointUrl;

    @Schema(description = "安全策略", requiredMode = Schema.RequiredMode.REQUIRED, example = "Basic256Sha256")
    private String securityPolicy;

    @Schema(description = "安全模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "SignAndEncrypt")
    private String securityMode;

    @Schema(description = "认证方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer authType;

    @Schema(description = "凭据别名", example = "kep-reader")
    private String credentialAlias;

    @Schema(description = "请求超时时间（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    private Integer timeout;

    @Schema(description = "重试间隔（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    private Integer retryInterval;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
