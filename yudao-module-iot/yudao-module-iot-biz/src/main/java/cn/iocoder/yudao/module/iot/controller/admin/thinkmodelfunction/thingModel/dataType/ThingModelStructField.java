package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType;

import lombok.Data;

@Data
public class ThingModelStructField {

    private String identifier;
    private String name;
    private ThingModelDataType dataType;
    private String description;

}