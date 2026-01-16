package cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// TODO @AI：不需要分页接口；
@Schema(description = "管理后台 - IoT 设备 Modbus 连接配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceModbusConfigPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "Modbus 服务器 IP 地址", example = "192.168.1.100")
    private String ip;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
