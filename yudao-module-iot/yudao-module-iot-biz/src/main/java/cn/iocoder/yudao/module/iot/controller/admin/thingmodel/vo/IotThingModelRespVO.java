package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo;

import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelService;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 产品物模型 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotThingModelRespVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21816")
    @ExcelProperty("产品ID")
    private Long id;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long productId;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature_sensor")
    @ExcelProperty("产品标识")
    private String productKey;

    @Schema(description = "功能标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature")
    private String identifier;

    @Schema(description = "功能名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温度")
    private String name;

    @Schema(description = "功能描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "测量当前环境温度")
    private String description;

    @Schema(description = "功能类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "属性", requiredMode = Schema.RequiredMode.REQUIRED)
    private ThingModelProperty property;

    @Schema(description = "服务", requiredMode = Schema.RequiredMode.REQUIRED)
    private ThingModelEvent event;

    @Schema(description = "事件", requiredMode = Schema.RequiredMode.REQUIRED)
    private ThingModelService service;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}