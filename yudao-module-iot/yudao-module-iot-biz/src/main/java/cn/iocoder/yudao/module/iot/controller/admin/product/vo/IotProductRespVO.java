package cn.iocoder.yudao.module.iot.controller.admin.product.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotProductRespVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26087")
    @ExcelProperty("产品编号")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品标识")
    private String productKey;

    @Schema(description = "接入网关协议", example = "2")
    @ExcelProperty("接入网关协议")
    private Integer protocolType;

    @Schema(description = "协议编号（脚本解析 id）", requiredMode = Schema.RequiredMode.REQUIRED, example = "13177")
    @ExcelProperty("协议编号（脚本解析 id）")
    private Long protocolId;

    @Schema(description = "产品所属品类标识符", example = "14237")
    @ExcelProperty("产品所属品类标识符")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你猜")
    @ExcelProperty("产品描述")
    private String description;

    @Schema(description = "数据校验级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("数据校验级别")
    private Integer validateType;

    @Schema(description = "产品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("产品状态")
    private Integer status;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("设备类型")
    private Integer deviceType;

    @Schema(description = "联网方式", example = "2")
    @ExcelProperty("联网方式")
    private Integer netType;

    @Schema(description = "数据格式")
    @ExcelProperty("数据格式")
    private Integer dataFormat;

}