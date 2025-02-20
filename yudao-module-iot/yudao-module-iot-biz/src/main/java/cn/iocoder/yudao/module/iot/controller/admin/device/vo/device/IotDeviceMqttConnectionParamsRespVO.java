package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备 MQTT 连接参数 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotDeviceMqttConnectionParamsRespVO {

    @Schema(description = "MQTT 客户端 ID", example = "24602")
    @ExcelProperty("MQTT 客户端 ID")
    private String mqttClientId;

    @Schema(description = "MQTT 用户名", example = "芋艿")
    @ExcelProperty("MQTT 用户名")
    private String mqttUsername;

    @Schema(description = "MQTT 密码")
    @ExcelProperty("MQTT 密码")
    private String mqttPassword;

}