package cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备 Modbus 点位配置 Response VO")
@Data
public class IotDeviceModbusPointRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceId;

    @Schema(description = "物模型属性编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long thingModelId;

    @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature")
    private String identifier;

    @Schema(description = "属性名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温度")
    private String name;

    @Schema(description = "Modbus 功能码", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer functionCode;

    @Schema(description = "寄存器起始地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer registerAddress;

    @Schema(description = "寄存器数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer registerCount;

    @Schema(description = "字节序", requiredMode = Schema.RequiredMode.REQUIRED, example = "AB")
    private String byteOrder;

    @Schema(description = "原始数据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "INT16")
    private String rawDataType;

    @Schema(description = "缩放因子", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0")
    private BigDecimal scale;

    @Schema(description = "轮询间隔（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    private Integer pollInterval;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
