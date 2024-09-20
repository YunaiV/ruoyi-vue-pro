package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备新增/修改 Request VO")
@Data
public class IotDeviceSaveReqVO {

    @Schema(description = "设备 ID，主键，自增", requiredMode = Schema.RequiredMode.REQUIRED, example = "177")
    private Long id;

    @Schema(description = "设备名称，在产品内唯一，用于标识设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    private String deviceName;

    @Schema(description = "设备备注名称，供用户自定义备注", example = "张三")
    private String nickname;

    @Schema(description = "产品 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26202")
    private Long productId;

}