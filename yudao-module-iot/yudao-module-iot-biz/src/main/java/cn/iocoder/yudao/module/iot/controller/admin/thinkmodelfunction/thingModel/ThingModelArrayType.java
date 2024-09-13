package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ThingModelArrayType extends ThingModelDataType {
    private ThingModelArraySpecs specs;
}

