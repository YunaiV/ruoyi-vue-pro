package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThingModelFloatType extends ThingModelDataType {
    private ThingModelFloatSpecs specs;
}

@Data
class ThingModelFloatSpecs {
    private Float min;
    private Float max;
    private Float step;
    private String unit;
}
