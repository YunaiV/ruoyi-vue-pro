package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelDataType;
import lombok.Data;

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
    // TODO @haohao：这个是不是 dataSpecs 和 dataSpecsList？https://help.aliyun.com/zh/iot/developer-reference/api-a99t11
    private ThingModelDataType dataType;

}
