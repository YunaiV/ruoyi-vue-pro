package cn.iocoder.yudao.module.iot.controller.admin.device.vo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备日志 Response VO")
@Data
public class IotDeviceLogRespVO {

    @Schema(description = "日志编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String id;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "product123")
    private String productKey;

    @Schema(description = "设备标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "device123")
    private String deviceKey;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "property")
    private String type;

    @Schema(description = "标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature")
    private String identifier;

    @Schema(description = "日志内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "上报时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime reportTime;

    @Schema(description = "记录时间戳", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime ts;

}