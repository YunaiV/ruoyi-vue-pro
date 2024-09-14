package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelArgument;
import lombok.Data;
import java.util.List;

@Data
public class ThingModelService {

    private String identifier;
    private String name;
    /**
     * 调用类型
     *
     * "sync"、"async"
     */
    private String callType;
    private List<ThingModelArgument> inputData;
    private List<ThingModelArgument> outputData;
    private String description;
    private String method;

}
