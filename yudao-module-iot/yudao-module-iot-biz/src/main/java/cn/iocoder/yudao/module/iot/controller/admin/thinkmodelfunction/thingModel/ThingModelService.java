package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelArgument;
import lombok.Data;
import java.util.List;

@Data
public class ThingModelService {

    /**
     * 服务标识符
     */
    private String identifier;
    /**
     * 服务名称
     */
    private String name;
    /**
     * 服务描述
     */
    private String description;

    /**
     * 调用类型
     *
     * "sync"、"async"
     */
    private String callType;
    private List<ThingModelArgument> inputData;
    private List<ThingModelArgument> outputData;
    private String method;

}
