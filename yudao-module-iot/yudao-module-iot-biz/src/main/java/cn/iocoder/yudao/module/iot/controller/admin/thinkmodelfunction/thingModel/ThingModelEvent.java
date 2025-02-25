package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelArgument;
import lombok.Data;
import java.util.List;

@Data
public class ThingModelEvent {

    /**
     * 事件标识符
     */
    private String identifier;
    /**
     * 事件名称
     */
    private String name;
    /**
     * 事件描述
     */
    private String description;

    /**
     * 事件类型
     *
     * "info"、"alert"、"error"
     */
    private String type;
    private List<ThingModelArgument> outputData;
    private String method;

}
