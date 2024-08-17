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

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "778")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品标识")
    private String identification;

    @Schema(description = "设备类型：device、gatway、gatway_sub", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("设备类型：device、gatway、gatway_sub")
    private String deviceType;

    @Schema(description = "厂商名称", example = "李四")
    @ExcelProperty("厂商名称")
    private String manufacturerName;

    @Schema(description = "产品型号")
    @ExcelProperty("产品型号")
    private String model;

    @Schema(description = "数据格式:1. 标准数据格式（JSON）2. 透传/自定义，脚本解析")
    @ExcelProperty("数据格式:1. 标准数据格式（JSON）2. 透传/自定义，脚本解析")
    private Integer dataFormat;

    @Schema(description = "设备接入平台的协议类型，默认为MQTT", example = "2")
    @ExcelProperty("设备接入平台的协议类型，默认为MQTT")
    private String protocolType;

    @Schema(description = "产品描述", example = "随便")
    @ExcelProperty("产品描述")
    private String description;

    @Schema(description = "产品状态 (0: 启用, 1: 停用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("产品状态 (0: 启用, 1: 停用)")
    private Integer status;

    @Schema(description = "物模型定义", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物模型定义")
    private String metadata;

    @Schema(description = "消息协议ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("消息协议ID")
    private Long messageProtocol;

    @Schema(description = "消息协议名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("消息协议名称")
    private String protocolName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}