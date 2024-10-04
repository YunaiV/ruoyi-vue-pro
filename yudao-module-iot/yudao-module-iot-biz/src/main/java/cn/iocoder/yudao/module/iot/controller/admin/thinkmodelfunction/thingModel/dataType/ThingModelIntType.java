package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType;

import lombok.Data;

@Data
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
