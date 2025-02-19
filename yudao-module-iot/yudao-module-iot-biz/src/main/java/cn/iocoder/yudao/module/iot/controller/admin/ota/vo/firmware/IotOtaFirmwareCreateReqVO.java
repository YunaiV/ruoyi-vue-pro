package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@Schema(description = "管理后台 - OTA固件创建 Request VO")
public class IotOtaFirmwareCreateReqVO extends IotOtaFirmwareCommonReqVO {

    /**
     * 版本号
     */
    @NotEmpty(message = "版本号不能为空")
    @Schema(description = "版本号", requiredMode = REQUIRED, example = "1.0.0")
    private String version;

    /**
     * 产品编号
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO#getId()}
     */
    @NotNull(message = "产品编号不能为空")
    @Schema(description = "产品编号", requiredMode = REQUIRED, example = "1024")
    private String productId;

    /**
     * 产品标识
     * <p>
     * 冗余 {@link cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO#getProductKey()}
     */
    @NotEmpty(message = "产品标识不能为空")
    @Schema(description = "产品标识", requiredMode = REQUIRED, example = "yudao")
    private String productKey;

    /**
     * 签名方式
     * <p>
     * 例如说：MD5、SHA256
     */
    @Schema(description = "签名方式", example = "MD5")
    private String signMethod;

    /**
     * 固件文件签名
     */
    @Schema(description = "固件文件签名", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String fileSign;

    /**
     * 固件文件大小
     */
    @NotNull(message = "固件文件大小不能为空")
    @Schema(description = "固件文件大小（单位：byte）", example = "1024")
    private Long fileSize;

    /**
     * 固件文件 URL
     */
    @NotEmpty(message = "固件文件 URL 不能为空")
    @Schema(description = "固件文件 URL", requiredMode = REQUIRED, example = "https://www.iocoder.cn/yudao-firmware.zip")
    private String fileUrl;

    /**
     * 自定义信息，建议使用 JSON 格式
     */
    @Schema(description = "自定义信息，建议使用 JSON 格式", example = "{\"key1\":\"value1\",\"key2\":\"value2\"}")
    private String information;

}
