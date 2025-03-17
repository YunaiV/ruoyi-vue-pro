package cn.iocoder.yudao.module.iot.controller.admin.device.vo.control;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 设备下行 Request VO") // 服务调用、属性设置、属性获取等
@Data
public class IotDeviceDownstreamReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "177")
    @NotNull(message = "设备编号不能为空")
    private Long id;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "property")
    @NotEmpty(message = "消息类型不能为空")
    @InEnum(IotDeviceMessageTypeEnum.class)
    private String type;

    @Schema(description = "标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "report")
    @NotEmpty(message = "标识符不能为空")
    private String identifier; // 参见 IotDeviceMessageIdentifierEnum 枚举类

    @Schema(description = "请求参数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Object data; // 例如说：服务调用的 params、属性设置的 properties

}
