package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThingModelIntType extends ThingModelDataType {
    private ThingModelIntSpecs specs;
}

@Data
class ThingModelIntSpecs {
    private Integer min;
    private Integer max;
    private Integer step;
    private String unit;
}
