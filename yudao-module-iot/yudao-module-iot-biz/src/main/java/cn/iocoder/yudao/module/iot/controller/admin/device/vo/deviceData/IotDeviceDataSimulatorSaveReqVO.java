package cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 模拟设备数据 Request VO")
@Data
public class IotDeviceDataSimulatorSaveReqVO {

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "msg123")
    private String id;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "product123")
    @NotEmpty(message = "产品ID不能为空")
    private String productKey;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "device123")
    @NotEmpty(message = "设备ID不能为空")
    private String deviceKey;

    @Schema(description = "消息/日志类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "property")
    @NotEmpty(message = "消息类型不能为空")
    private String type;

    @Schema(description = "标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature")
    @NotEmpty(message = "标识符不能为空")
    private String subType;

    @Schema(description = "数据内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"value\": 25.6}")
    @NotEmpty(message = "数据内容不能为空")
    private String content;

    @Schema(description = "上报时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reportTime;

}
