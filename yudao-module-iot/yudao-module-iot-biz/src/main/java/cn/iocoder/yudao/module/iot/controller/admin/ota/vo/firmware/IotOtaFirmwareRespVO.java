package cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware;

import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理后台 - IoT OTA 固件 Response VO")
public class IotOtaFirmwareRespVO implements VO {

    /**
     * 固件编号
     */
    @Schema(description = "固件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;
    /**
     * 固件名称
     */
    @Schema(description = "固件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "OTA固件")
    private String name;
    /**
     * 固件描述
     */
    @Schema(description = "固件描述")
    private String description;
    /**
     * 版本号
     */
    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0.0")
    private String version;

    /**
     * 产品编号
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO#getId()}
     */
    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @Trans(type = TransType.SIMPLE, target = IotProductDO.class, fields = {"name"}, refs = {"productName"})
    private String productId;
    /**
     * 产品标识
     * <p>
     * 冗余 {@link cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO#getProductKey()}
     */
    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "iot-product-key")
    private String productKey;
    /**
     * 产品名称
     */
    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "OTA产品")
    private String productName;
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
    @Schema(description = "固件文件签名", example = "1024")
    private String fileSign;
    /**
     * 固件文件大小
     */
    @Schema(description = "固件文件大小", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long fileSize;
    /**
     * 固件文件 URL
     */
    @Schema(description = "固件文件 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    private String fileUrl;
    /**
     * 自定义信息，建议使用 JSON 格式
     */
    @Schema(description = "自定义信息，建议使用 JSON 格式")
    private String information;

}
