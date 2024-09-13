package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - IoT 产品物模型 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotThinkModelFunctionRespVO {

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21816")
    @ExcelProperty("产品ID")
    private Long id;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品标识")
    private String productKey;

    @Schema(description = "属性列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("属性列表")
    private List<IotThingModelProperty> properties;

    @Schema(description = "服务列表")
    @ExcelProperty("服务列表")
    private String services;

    @Schema(description = "事件列表")
    @ExcelProperty("事件列表")
    private String events;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}