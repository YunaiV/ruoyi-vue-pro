package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.dataType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * IoT 物模型数据类型为数组的 DataSpec 定义
 *
 * @author HUIHUI
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"dataType"}) // 忽略子类中的 dataType 字段，从而避免重复
public class ThingModelArrayDataSpecs extends ThingModelDataSpecs {

    /**
     * 数组中的元素个数
     */
    private Integer size;
    /**
     * 数组中的元素的数据类型。可选值：struct、int、float、double 或 text
     */
    private String childDataType;
    /**
     * 数据类型（childDataType）为列表型 struct 的数据规范存储在 dataSpecsList 中
     * 此时 struct 取值范围为：int、float、double、text、date、enum、bool
     */
    private List<ThingModelDataSpecs> dataSpecsList;

}

