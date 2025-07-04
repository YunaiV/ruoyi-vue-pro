package cn.iocoder.yudao.module.iot.controller.admin.product.vo.product;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotLocationTypeEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotNetTypeEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 产品新增/修改 Request VO")
@Data
public class IotProductSaveReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.AUTO, example = "1")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温湿度")
    @NotEmpty(message = "产品名称不能为空")
    private String name;

    @Schema(description = "产品 Key", requiredMode = Schema.RequiredMode.AUTO, example = "12345abc")
    private String productKey;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品分类编号不能为空")
    private Long categoryId;

    @Schema(description = "产品图标", example = "https://iocoder.cn/1.svg")
    private String icon;

    @Schema(description = "产品图片", example = "https://iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "产品描述", example = "描述")
    private String description;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotProductDeviceTypeEnum.class, message = "设备类型必须是 {value}")
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    @Schema(description = "联网方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotNetTypeEnum.class, message = "联网方式必须是 {value}")
    private Integer netType;

    @Schema(description = "定位类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotLocationTypeEnum.class, message = "定位方式必须是 {value}")
    private Integer locationType;

    @Schema(description = "数据格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "数据格式不能为空")
    private String codecType;

}