package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType;

import lombok.Data;

@Data
public class ThingModelArraySpecs {

    /**
     * 数组长度
     */
    private int size;
    /**
     * 数组元素的类型
     */
    private ThingModelDataType item;

}
