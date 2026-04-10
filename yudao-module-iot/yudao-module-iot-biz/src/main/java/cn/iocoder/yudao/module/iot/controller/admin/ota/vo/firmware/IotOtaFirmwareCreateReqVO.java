package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Schema(description = "管理后台 - IoT OTA 固件创建 Request VO")
@Data
public class IotOtaFirmwareCreateReqVO {

    @Schema(description = "固件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "智能开关固件")
    @NotEmpty(message = "固件名称不能为空")
    private String name;

    @Schema(description = "固件描述", example = "某品牌型号固件，测试用")
    private String description;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0.0")
    @NotEmpty(message = "版本号不能为空")
    private String version;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "产品编号不能为空")
    private Long productId;

    @Schema(description = "固件文件 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.zip")
    @NotEmpty(message = "固件文件 URL 不能为空")
    @URL(message = "固件文件 URL 格式错误")
    private String fileUrl;

}
