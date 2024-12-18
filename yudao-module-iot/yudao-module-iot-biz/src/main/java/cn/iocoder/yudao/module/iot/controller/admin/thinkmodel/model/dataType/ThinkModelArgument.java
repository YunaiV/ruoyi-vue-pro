package cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.dataType;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.ThinkModelProperty;
import lombok.Data;

@Data
public class ThinkModelArgument {

    public static final String DIRECTION_INPUT = "input";
    public static final String DIRECTION_OUTPUT = "output";

    private String identifier;
    private String name;
    /**
     * 物模型中的属性
     */
    private ThinkModelProperty property;
    /**
     * 用于区分输入或输出参数，"input" 或 "output"
     */
    private String direction;
    private String description;

}
