package cn.iocoder.yudao.module.iot.controller.admin.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - iot 产品新增/修改 Request VO")
@Data
public class ProductSaveReqVO {

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "产品名称不能为空")
    private String name;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26087")
    private Long id;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;

    @Schema(description = "协议编号（脚本解析 id）", requiredMode = Schema.RequiredMode.REQUIRED, example = "13177")
    @NotNull(message = "协议编号（脚本解析 id）不能为空")
    private Long protocolId;

    @Schema(description = "产品所属品类标识符", example = "14237")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你猜")
    private String description;

    @Schema(description = "数据校验级别, 0: 强校验, 1: 弱校验, 2: 免校验", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数据校验级别, 0: 强校验, 1: 弱校验, 2: 免校验不能为空")
    private Integer validateType;

    @Schema(description = "产品状态, 0: DEVELOPMENT_STATUS, 1: RELEASE_STATUS", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品状态, 0: DEVELOPMENT_STATUS, 1: RELEASE_STATUS不能为空")
    private Integer status;

    @Schema(description = "设备类型, 0: 直连设备, 1: 网关子设备, 2: 网关设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "设备类型, 0: 直连设备, 1: 网关子设备, 2: 网关设备不能为空")
    private Integer deviceType;

    @Schema(description = "联网方式, 0: Wi-Fi, 1: Cellular, 2: Ethernet, 3: 其他", example = "2")
    private Integer netType;

    @Schema(description = "接入网关协议, 0: modbus, 1: opc-ua, 2: customize, 3: ble, 4: zigbee", example = "2")
    private Integer protocolType;

    @Schema(description = "数据格式, 0: 透传模式, 1: Alink JSON")
    private Integer dataFormat;

}