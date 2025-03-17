package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Set;

@Schema(description = "管理后台 - IoT 设备新增/修改 Request VO")
@Data
public class IotDeviceSaveReqVO {

    @Schema(description = "设备编号", example = "177")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.AUTO, example = "177")
    @Size(max = 50, message = "设备编号长度不能超过 50 个字符")
    private String deviceKey;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.AUTO, example = "王五")
    private String deviceName;

    @Schema(description = "备注名称", example = "张三")
    private String nickname;

    @Schema(description = "设备序列号", example = "123456")
    private String serialNumber;

    @Schema(description = "设备图片", example = "https://iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "设备分组编号数组", example = "1,2")
    private Set<Long> groupIds;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26202")
    private Long productId;

    @Schema(description = "网关设备 ID", example = "16380")
    private Long gatewayId;

    @Schema(description = "设备配置", example = "{\"abc\": \"efg\"}")
    private String config;

}