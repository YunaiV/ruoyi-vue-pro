package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType;

import lombok.Data;

import java.util.Map;

@Data
public class ThingModelEnumType extends ThingModelDataType {

    /**
     * 枚举值和描述的键值对
     */
    private Map<String, String> specs;

}
