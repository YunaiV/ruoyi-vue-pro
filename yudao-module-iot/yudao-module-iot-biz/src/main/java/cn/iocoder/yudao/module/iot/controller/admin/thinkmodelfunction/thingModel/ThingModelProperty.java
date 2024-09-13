package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;

@Data
public class ThingModelProperty {
    private String identifier;
    private String name;
    private String accessMode; // "rw"、"r"、"w"
    private boolean required;
    private ThingModelDataType dataType;
    private String description;
}
