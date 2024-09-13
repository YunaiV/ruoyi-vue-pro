package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;
import java.util.List;

@Data
public class ThingModelEvent {
    private String identifier;
    private String name;
    private String type; // "info"、"alert"、"error"
    private List<ThingModelArgument> outputData;
    private String description;
    private String method;
}
