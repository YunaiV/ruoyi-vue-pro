package cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.dataType.ThinkModelArgument;
import lombok.Data;
import java.util.List;

@Data
public class ThinkModelService {

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
    private List<ThinkModelArgument> inputData;
    private List<ThinkModelArgument> outputData;
    private String method;

}
