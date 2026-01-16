package cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT 设备 Modbus 点位配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceModbusPointPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "属性标识符", example = "temperature")
    private String identifier;

    @Schema(description = "属性名称", example = "温度")
    private String name;

    @Schema(description = "Modbus 功能码", example = "3")
    private Integer functionCode;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
