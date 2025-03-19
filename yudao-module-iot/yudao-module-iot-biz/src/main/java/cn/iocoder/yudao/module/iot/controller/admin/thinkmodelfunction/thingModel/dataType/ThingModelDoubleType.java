package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType;

import lombok.Data;

@Data
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
