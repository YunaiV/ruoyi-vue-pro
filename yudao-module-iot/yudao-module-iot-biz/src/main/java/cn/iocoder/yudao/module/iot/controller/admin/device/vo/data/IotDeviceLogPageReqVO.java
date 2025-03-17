package cn.iocoder.yudao.module.iot.controller.admin.device.vo.data;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - IoT 设备日志分页查询 Request VO")
@Data
public class IotDeviceLogPageReqVO extends PageParam {

    @Schema(description = "设备标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "device123")
    @NotEmpty(message = "设备标识不能为空")
    private String deviceKey;

    @Schema(description = "消息类型", example = "property")
    private String type; // 参见 IotDeviceMessageTypeEnum 枚举，精准匹配

    @Schema(description = "标识符", example = "temperature")
    private String identifier; // 参见 IotDeviceMessageIdentifierEnum 枚举，模糊匹配

}