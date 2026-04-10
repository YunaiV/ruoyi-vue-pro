package cn.iocoder.yudao.module.iot.controller.admin.device.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备消息对 Response VO")
@Data
public class IotDeviceMessageRespPairVO {

    @Schema(description = "请求消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private IotDeviceMessageRespVO request;

    @Schema(description = "响应消息")
    private IotDeviceMessageRespVO reply; // 通过 requestId 配对

}
