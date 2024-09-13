package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;
import java.util.List;

@Data
public class ThingModelService {
    private String identifier;
    private String name;
    private String callType; // "sync"、"async"
    private List<ThingModelArgument> inputData;
    private List<ThingModelArgument> outputData;
    private String description;
    private String method;
}
