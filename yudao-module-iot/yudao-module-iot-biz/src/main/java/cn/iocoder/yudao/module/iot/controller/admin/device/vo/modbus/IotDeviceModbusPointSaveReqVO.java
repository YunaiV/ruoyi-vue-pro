package cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 设备 Modbus 点位配置新增/修改 Request VO")
@Data
public class IotDeviceModbusPointSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "物模型属性编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "物模型属性编号不能为空")
    private Long thingModelId;

    @Schema(description = "Modbus 功能码", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "Modbus 功能码不能为空")
    private Integer functionCode;

    @Schema(description = "寄存器起始地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "寄存器起始地址不能为空")
    private Integer registerAddress;

    @Schema(description = "寄存器数量", example = "1")
    private Integer registerCount;

    @Schema(description = "位索引（0-15，0 为最低位；仅 FC03/FC04、BOOLEAN、1 个寄存器的点位；为空表示整个寄存器）", example = "8")
    @Min(value = 0, message = "位索引不能小于 0")
    @Max(value = 15, message = "位索引不能大于 15")
    private Integer bitIndex;

    @Schema(description = "字节序", requiredMode = Schema.RequiredMode.REQUIRED, example = "AB")
    @NotEmpty(message = "字节序不能为空")
    private String byteOrder;

    @Schema(description = "原始数据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "INT16")
    @NotEmpty(message = "原始数据类型不能为空")
    private String rawDataType;

    @Schema(description = "缩放因子", example = "1.0")
    private BigDecimal scale;

    @Schema(description = "轮询间隔（毫秒）", example = "5000")
    @Min(value = 100, message = "轮询间隔不能小于 100 毫秒")
    private Integer pollInterval;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
