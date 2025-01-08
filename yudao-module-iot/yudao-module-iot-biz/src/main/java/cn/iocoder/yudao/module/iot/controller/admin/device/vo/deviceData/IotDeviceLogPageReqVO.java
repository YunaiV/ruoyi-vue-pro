package cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 设备日志分页查询 Request VO")
@Data
public class IotDeviceLogPageReqVO extends PageParam {

    @Schema(description = "设备标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "device123")
    @NotEmpty(message = "设备标识不能为空")
    private String deviceKey;

    @Schema(description = "消息类型", example = "property")
    private String type;

    @Schema(description = "标识符", example = "temperature")
    private String subType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
} 