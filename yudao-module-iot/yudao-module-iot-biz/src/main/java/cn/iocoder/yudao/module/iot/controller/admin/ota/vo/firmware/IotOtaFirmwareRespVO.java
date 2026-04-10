package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware;

import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT OTA 固件 Response VO")
@Data
public class IotOtaFirmwareRespVO implements VO {

    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "固件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "OTA 固件")
    private String name;

    @Schema(description = "固件描述")
    private String description;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0.0")
    private String version;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long productId;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "智能设备")
    private String productName;

    @Schema(description = "固件文件 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/firmware.bin")
    private String fileUrl;

    @Schema(description = "固件文件大小", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long fileSize;

    @Schema(description = "固件文件签名算法", example = "MD5")
    private String fileDigestAlgorithm;

    @Schema(description = "固件文件签名结果", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String fileDigestValue;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
