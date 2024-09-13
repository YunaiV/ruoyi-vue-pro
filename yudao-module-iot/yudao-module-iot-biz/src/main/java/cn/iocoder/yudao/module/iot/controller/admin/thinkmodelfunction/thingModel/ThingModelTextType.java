package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThingModelTextType extends ThingModelDataType {
    private ThingModelTextSpecs specs;
}

@Data
class ThingModelTextSpecs {
    private Integer length; // 最大长度
}
