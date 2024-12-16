package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelDataType;
import lombok.Data;

import java.util.List;

@Data
public class ThingModelProperty {

    /**
     * 属性标识符
     */
    private String identifier;
    /**
     * 属性名称
     */
    private String name;
    /**
     * 属性描述
     */
    private String description;

    private String accessMode; // "rw"、"r"、"w"
    private Boolean required;
    private ThingModelDataType dataSpecs;
    private List<ThingModelDataType> dataSpecsList;

}
