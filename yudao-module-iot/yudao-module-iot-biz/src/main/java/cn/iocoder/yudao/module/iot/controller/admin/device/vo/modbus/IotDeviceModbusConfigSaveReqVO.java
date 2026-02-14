package cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 设备 Modbus 连接配置新增/修改 Request VO")
@Data
public class IotDeviceModbusConfigSaveReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "Modbus 服务器 IP 地址", example = "192.168.1.100")
    private String ip;

    @Schema(description = "Modbus 端口", example = "502")
    private Integer port;

    @Schema(description = "从站地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "从站地址不能为空")
    private Integer slaveId;

    @Schema(description = "连接超时时间（毫秒）", example = "3000")
    private Integer timeout;

    @Schema(description = "重试间隔（毫秒）", example = "1000")
    private Integer retryInterval;

    @Schema(description = "工作模式", example = "1")
    @InEnum(IotModbusModeEnum.class)
    private Integer mode;

    @Schema(description = "数据帧格式", example = "1")
    @InEnum(IotModbusFrameFormatEnum.class)
    private Integer frameFormat;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
