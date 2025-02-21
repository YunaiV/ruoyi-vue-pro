package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType;

import lombok.Data;

import java.util.List;

@Data
public class ThingModelStructType extends ThingModelDataType {

    private List<ThingModelStructField> specs;

}


