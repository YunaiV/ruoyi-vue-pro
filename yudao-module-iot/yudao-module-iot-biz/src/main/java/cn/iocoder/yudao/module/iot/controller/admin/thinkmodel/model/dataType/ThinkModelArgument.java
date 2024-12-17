package cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.dataType;

import lombok.Data;

@Data
public class ThinkModelArgument {

    private String identifier;
    private String name;
    private ThinkModelDataSpecs dataType;
    /**
     * 用于区分输入或输出参数，"input" 或 "output"
     */
    private String direction;
    private String description;

}
