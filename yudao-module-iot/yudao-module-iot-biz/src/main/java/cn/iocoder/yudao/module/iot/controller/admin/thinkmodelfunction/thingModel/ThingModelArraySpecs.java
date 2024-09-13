package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel;

import lombok.Data;

@Data
public class ThingModelArraySpecs {
    private int size; // 数组长度
    private ThingModelDataType item; // 数组元素的类型
}
