package cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备 Modbus 连接配置 Response VO")
@Data
public class IotDeviceModbusConfigRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceId;

    @Schema(description = "设备名称", example = "温湿度传感器")
    private String deviceName;

    @Schema(description = "Modbus 服务器 IP 地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "192.168.1.100")
    private String ip;

    @Schema(description = "Modbus 端口", requiredMode = Schema.RequiredMode.REQUIRED, example = "502")
    private Integer port;

    @Schema(description = "从站地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer slaveId;

    @Schema(description = "连接超时时间（毫秒）", example = "3000")
    private Integer timeout;

    @Schema(description = "重试间隔（毫秒）", example = "1000")
    private Integer retryInterval;

    @Schema(description = "工作模式", example = "1")
    private Integer mode;

    @Schema(description = "数据帧格式", example = "1")
    private Integer frameFormat;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
