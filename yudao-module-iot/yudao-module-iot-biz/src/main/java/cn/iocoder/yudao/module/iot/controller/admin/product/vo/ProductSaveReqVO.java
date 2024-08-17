package cn.iocoder.yudao.module.iot.controller.admin.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - iot 产品新增/修改 Request VO")
@Data
public class ProductSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "778")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温湿度")
    @NotEmpty(message = "产品名称不能为空")
    private String name;

    @Schema(description = "产品标识", example = "123456")
    private String identification;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "device")
    @NotEmpty(message = "设备类型不能为空")
    private String deviceType;

    @Schema(description = "数据格式:1. 标准数据格式（JSON）2. 透传/自定义", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "数据格式不能为空")
    private Integer dataFormat;

    @Schema(description = "设备接入平台的协议类型，默认为MQTT", requiredMode = Schema.RequiredMode.REQUIRED, example = "mqtt")
    @NotEmpty(message = "设备接入平台的协议类型不能为空")
    private String protocolType;

    @Schema(description = "厂商名称", example = "电信")
    private String manufacturerName;

    @Schema(description = "产品型号", example = "wsd-01")
    private String model;

    @Schema(description = "产品描述", example = "随便")
    private String description;

//    @Schema(description = "产品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
//    private Integer status;
//
//    @Schema(description = "物模型定义", requiredMode = Schema.RequiredMode.REQUIRED)
//    private String metadata;
//
//    @Schema(description = "消息协议ID", requiredMode = Schema.RequiredMode.REQUIRED)
//    private Long messageProtocol;
//
//    @Schema(description = "消息协议名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
//    private String protocolName;

}