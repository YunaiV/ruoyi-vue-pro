package cn.iocoder.yudao.module.iot.controller.admin.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - iot 产品新增/修改 Request VO")
@Data
public class ProductSaveReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.AUTO, example = "1")
    private Long id;

    @Schema(description = "产品Key", requiredMode = Schema.RequiredMode.AUTO, example = "12345abc")
    private String productKey;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温湿度")
    @NotEmpty(message = "产品名称不能为空")
    private String name;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    @Schema(description = "联网方式", requiredMode = Schema.RequiredMode.REQUIRED,example = "0")
    @NotNull(message = "联网方式不能为空")
    private Integer netType;

    @Schema(description = "接入网关协议", requiredMode = Schema.RequiredMode.REQUIRED,example = "0")
    @NotNull(message = "接入网关协议不能为空")
    private Integer protocolType;

    @Schema(description = "数据格式",requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "数据格式不能为空")
    private Integer dataFormat;

    @Schema(description = "数据校验级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "数据校验级别不能为空")
    private Integer validateType;

    @Schema(description = "产品描述", example = "描述")
    private String description;

}