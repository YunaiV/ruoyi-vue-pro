package cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.dataType;

import lombok.Data;

import java.util.List;

/**
 * 物模型数据类型为数组的 DataSpec 定义
 *
 * @author HUIHUI
 */
@Data
public class ThingModelArrayDataSpecs extends ThingModelDataSpecs {

    /**
     * 数组中的元素个数。
     */
    private Long size;
    /**
     * 数组中的元素的数据类型。可选值：struct、int、float、double 或 text。
     */
    private String childDataType;
    /**
     * 数据类型（dataType）为非列表型（int、float、double、text、date、array）的数据规范存储在 dataSpecs 中。
     * 仅当 dataType 不是列表型时，才传入此字段。
     */
    private ThingModelDataSpecs dataSpecs;
    /**
     * 数据类型（dataType）为列表型（enum、bool、struct）的数据规范存储在 dataSpecsList 中。
     * 仅当 dataType 是列表型时，才传入此字段。
     */
    private List<ThingModelDataSpecs> dataSpecsList;

}

