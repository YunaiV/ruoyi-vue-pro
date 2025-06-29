package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo;

import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelEvent;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 产品物模型 TSL Response VO")
@Data
public class IotThingModelTSLRespVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long productId;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature_sensor")
    private String productKey;

    @Schema(description = "属性列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ThingModelProperty> properties;

    @Schema(description = "服务列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ThingModelEvent> events;

    @Schema(description = "事件列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ThingModelService> services;

}