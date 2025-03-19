package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType;

import lombok.Data;

@Data
public class ThingModelArgument {

    private String identifier;
    private String name;
    private ThingModelDataType dataType;
    /**
     * 用于区分输入或输出参数，"input" 或 "output"
     */
    private String direction;
    private String description;

}
