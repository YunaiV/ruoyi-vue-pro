package cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.dataType;

import lombok.Data;

@Data
public class ThingModelArgument {

    private String identifier;
    private String name;
    private ThingModelDataSpecs dataType;
    /**
     * 用于区分输入或输出参数，"input" 或 "output"
     */
    private String direction;
    private String description;

}
