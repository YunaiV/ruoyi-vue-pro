package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;

@Data
public class ThingModelArgument {
    private String identifier;
    private String name;
    private ThingModelDataType dataType;
    private String direction; // 用于区分输入或输出参数，"input" 或 "output"
    private String description;
}
