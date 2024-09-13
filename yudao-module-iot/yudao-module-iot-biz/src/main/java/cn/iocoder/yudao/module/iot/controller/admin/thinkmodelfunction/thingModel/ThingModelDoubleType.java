package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThingModelDoubleType extends ThingModelDataType {
    private ThingModelDoubleSpecs specs;
}

@Data
class ThingModelDoubleSpecs {
    private Double min;
    private Double max;
    private Double step;
    private String unit;
}
