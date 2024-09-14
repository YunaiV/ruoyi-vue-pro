package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelArgument;
import lombok.Data;
import java.util.List;

@Data
public class ThingModelEvent {

    private String identifier;
    private String name;
    /**
     * 事件类型
     *
     * "info"、"alert"、"error"
     */
    private String type;
    private List<ThingModelArgument> outputData;
    private String description;
    private String method;

}
