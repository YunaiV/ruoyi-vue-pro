package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelDataType;
import lombok.Data;

@Data
public class ThingModelProperty {

    private String identifier;
    private String name;
    private String accessMode; // "rw"、"r"、"w"
    private Boolean required;
    private ThingModelDataType dataType;
    private String description;

}
