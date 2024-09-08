package cn.iocoder.yudao.module.iot.controller.admin.product.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.product.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - iot 产品新增/修改 Request VO")
@Data
public class IotProductSaveReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.AUTO, example = "1")
    private Long id;

    @Schema(description = "产品Key", requiredMode = Schema.RequiredMode.AUTO, example = "12345abc")
    private String productKey;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温湿度")
    @NotEmpty(message = "产品名称不能为空")
    private String name;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotProductDeviceTypeEnum.class, message = "设备类型必须是 {value}")
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    @Schema(description = "联网方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotNetTypeEnum.class, message = "联网方式必须是 {value}")
    private Integer netType;

    @Schema(description = "接入网关协议", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotProtocolTypeEnum.class, message = "接入网关协议必须是 {value}")
    private Integer protocolType;

    @Schema(description = "数据格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotDataFormatEnum.class, message = "数据格式必须是 {value}")
    @NotNull(message = "数据格式不能为空")
    private Integer dataFormat;

    @Schema(description = "数据校验级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotValidateTypeEnum.class, message = "数据校验级别必须是 {value}")
    @NotNull(message = "数据校验级别不能为空")
    private Integer validateType;

    @Schema(description = "产品描述", example = "描述")
    private String description;

}