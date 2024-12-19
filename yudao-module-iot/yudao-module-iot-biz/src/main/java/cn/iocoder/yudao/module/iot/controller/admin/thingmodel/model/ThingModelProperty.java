package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model;

import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.dataType.ThingModelDataSpecs;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotProductThingModelAccessModeEnum;
import lombok.Data;

import java.util.List;

/**
 * 物模型中的属性
 *
 * dataSpecs 和 dataSpecsList 之中必须传入且只能传入一个
 *
 * @author HUIHUI
 */
@Data
public class ThingModelProperty {

    /**
     * 属性标识符
     */
    private String identifier;
    /**
     * 属性名称
     */
    private String name;
    /**
     * 属性描述
     */
    private String description;
    /**
     * 云端可以对该属性进行的操作类型
     * 关联枚举 {@link IotProductThingModelAccessModeEnum}
     */
    private String accessMode;
    /**
     * 是否是标准品类的必选服务。
     *
     * - true：是
     * - false：否
     */
    private Boolean required;
    /**
     * 数据类型，与 dataSpecs 的 dataType 保持一致
     */
    private String dataType;
    /**
     * 数据类型（dataType）为非列表型（int、float、double、text、date、array）的数据规范存储在 dataSpecs 中
     */
    private ThingModelDataSpecs dataSpecs;
    /**
     * 数据类型（dataType）为列表型（enum、bool、struct）的数据规范存储在 dataSpecsList 中
     */
    private List<ThingModelDataSpecs> dataSpecsList;

}
