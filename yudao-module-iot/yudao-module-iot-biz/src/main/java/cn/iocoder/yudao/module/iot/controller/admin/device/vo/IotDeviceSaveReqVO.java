package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备新增/修改 Request VO")
@Data
public class IotDeviceSaveReqVO {

    @Schema(description = "设备编号", example = "177")
    private Long id;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    private String deviceName;

    @Schema(description = "备注名称", example = "张三")
    private String nickname;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26202")
    private Long productId;

}