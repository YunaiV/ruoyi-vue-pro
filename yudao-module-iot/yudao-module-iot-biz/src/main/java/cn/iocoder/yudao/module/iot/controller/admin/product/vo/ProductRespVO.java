package cn.iocoder.yudao.module.iot.controller.admin.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - iot 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductRespVO {

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26087")
    @ExcelProperty("产品ID")
    private Long id;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品标识")
    private String productKey;

    @Schema(description = "协议编号（脚本解析 id）", requiredMode = Schema.RequiredMode.REQUIRED, example = "13177")
    @ExcelProperty("协议编号（脚本解析 id）")
    private Long protocolId;

    @Schema(description = "产品所属品类标识符", example = "14237")
    @ExcelProperty("产品所属品类标识符")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你猜")
    @ExcelProperty("产品描述")
    private String description;

    @Schema(description = "数据校验级别, 0: 强校验, 1: 弱校验, 2: 免校验", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("数据校验级别, 0: 强校验, 1: 弱校验, 2: 免校验")
    private Integer validateType;

    @Schema(description = "产品状态, 0: DEVELOPMENT_STATUS, 1: RELEASE_STATUS", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("产品状态, 0: DEVELOPMENT_STATUS, 1: RELEASE_STATUS")
    private Integer status;

    @Schema(description = "设备类型, 0: 直连设备, 1: 网关子设备, 2: 网关设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("设备类型, 0: 直连设备, 1: 网关子设备, 2: 网关设备")
    private Integer deviceType;

    @Schema(description = "联网方式, 0: Wi-Fi, 1: Cellular, 2: Ethernet, 3: 其他", example = "2")
    @ExcelProperty("联网方式, 0: Wi-Fi, 1: Cellular, 2: Ethernet, 3: 其他")
    private Integer netType;

    @Schema(description = "接入网关协议, 0: modbus, 1: opc-ua, 2: customize, 3: ble, 4: zigbee", example = "2")
    @ExcelProperty("接入网关协议, 0: modbus, 1: opc-ua, 2: customize, 3: ble, 4: zigbee")
    private Integer protocolType;

    @Schema(description = "数据格式, 0: 透传模式, 1: Alink JSON")
    @ExcelProperty("数据格式, 0: 透传模式, 1: Alink JSON")
    private Integer dataFormat;

}