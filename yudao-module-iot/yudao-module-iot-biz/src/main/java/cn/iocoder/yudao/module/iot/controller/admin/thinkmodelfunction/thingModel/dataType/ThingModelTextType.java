package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType;

import lombok.Data;

@Data
public class ThingModelTextType extends ThingModelDataType {

    private ThingModelTextSpecs specs;

}

@Data
class ThingModelTextSpecs {

    /**
     * 最大长度
     */
    private Integer length;

}
